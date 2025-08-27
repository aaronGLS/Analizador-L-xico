package controller;

import model.config.Config;
import model.config.CommentsConfig;
import view.dialogs.ConfigDialog;
import core.io.ConfigLoader;
import core.io.ConfigSaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controlador responsable de orquestar la edición de la configuración léxica
 * (palabras reservadas, operadores, puntuación, agrupación y comentarios)
 * desde la UI, persistirla a disco y propagar el cambio al resto de la app.
 *
 * <p>
 * Reglas derivadas de la práctica/esquema:
 * <ul>
 * <li>La configuración es editable desde un diálogo de UI.</li>
 * <li>Tras guardar, la app debe usar la nueva configuración para análisis y
 * resaltado.</li>
 * <li>Validaciones mínimas: no permitir marcadores de comentario vacíos;
 * normalizar listas.</li>
 * </ul>
 */
public class ConfigController {

    private final Component parent;
    private final Config config; // referencia viva usada por el resto de la app
    private final ConfigLoader configLoader; // lectura de config.json (si hace falta recargar)
    private final ConfigSaver configSaver; // persistencia de config.json
    private final Path configPath = Paths.get("resources", "config.json"); // ruta por defecto

    /**
     * Callback opcional a ejecutar cuando la configuración cambia.
     * Útil para re-resaltar o re-analizar sin acoplar a servicios específicos.
     */
    private Runnable onConfigChanged;

    public ConfigController(Component parent,
            Config config,
            ConfigLoader configLoader,
            ConfigSaver configSaver) {
        this.parent = Objects.requireNonNull(parent, "parent");
        this.config = Objects.requireNonNull(config, "config");
        this.configLoader = Objects.requireNonNull(configLoader, "configLoader");
        this.configSaver = Objects.requireNonNull(configSaver, "configSaver");
    }

    /** Permite registrar una acción a ejecutar tras un guardado exitoso. */
    public void setOnConfigChanged(Runnable onConfigChanged) {
        this.onConfigChanged = onConfigChanged;
    }

    /*
     * =====================================================================
     * Flujo principal: abrir diálogo, editar, validar y guardar
     * =====================================================================
     */

    /** Abre el diálogo de configuración, valida y persiste los cambios. */
    public void openDialogAndSave() {
        // 1) Instanciar y precargar el diálogo
        Frame frameParent = JOptionPane.getFrameForComponent(parent);
        ConfigDialog dialog = new ConfigDialog(frameParent, true);
        preloadDialog(dialog, config);

        // Conectar acciones para editar listas (Agregar/Editar)
        attachListHandlers(dialog);

        // Acciones de botones inferiores
        dialog.getBtnCancelar().addActionListener(e -> dialog.dispose());

        dialog.getBtnAplicar().addActionListener(e -> saveConfig(dialog));
        dialog.getBtnAceptar().addActionListener(e -> {
            if (saveConfig(dialog)) {
                dialog.dispose();
            }
        });

        // 2) Mostrar de forma modal y esperar acción del usuario
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /** Configura listeners para agregar/editar elementos de cada lista. */
    private void attachListHandlers(ConfigDialog dialog) {
        dialog.setOnAddReservada(() -> addItem(dialog.getLstReservadas(), "palabra reservada", dialog));
        dialog.setOnEditReservada(() -> editItem(dialog.getLstReservadas(), "palabra reservada", dialog));

        dialog.setOnAddOperador(() -> addItem(dialog.getLstOperadores(), "operador", dialog));
        dialog.setOnEditOperador(() -> editItem(dialog.getLstOperadores(), "operador", dialog));

        dialog.setOnAddPuntuacion(() -> addItem(dialog.getLstPuntuacion(), "símbolo de puntuación", dialog));
        dialog.setOnEditPuntuacion(() -> editItem(dialog.getLstPuntuacion(), "símbolo de puntuación", dialog));

        dialog.setOnAddAgrupacion(() -> addItem(dialog.getLstAgrupacion(), "símbolo de agrupación", dialog));
        dialog.setOnEditAgrupacion(() -> editItem(dialog.getLstAgrupacion(), "símbolo de agrupación", dialog));
    }

    /** Añade un nuevo ítem a la lista verificando duplicados. */
    private void addItem(JList<String> list, String label, Component parentComp) {
        String value = askValue("Ingrese " + label + ":", "Agregar", parentComp, null);
        if (value == null) return;
        DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
        if (containsValue(model, value, -1)) {
            JOptionPane.showMessageDialog(parentComp,
                    "Valor duplicado: '" + value + "'", "Duplicado", JOptionPane.ERROR_MESSAGE);
            return;
        }
        model.addElement(value);
        list.setSelectedIndex(model.getSize() - 1);
    }

    /** Edita el ítem seleccionado verificando duplicados. */
    private void editItem(JList<String> list, String label, Component parentComp) {
        int idx = list.getSelectedIndex();
        if (idx < 0) return;
        DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
        String current = model.get(idx);
        String value = askValue("Editar " + label + ":", "Editar", parentComp, current);
        if (value == null) return;
        if (containsValue(model, value, idx)) {
            JOptionPane.showMessageDialog(parentComp,
                    "Valor duplicado: '" + value + "'", "Duplicado", JOptionPane.ERROR_MESSAGE);
            return;
        }
        model.set(idx, value);
        list.setSelectedIndex(idx);
    }

    /** Diálogo genérico para ingresar un valor. */
    private String askValue(String message, String title, Component parentComp, String current) {
        String val = (String) JOptionPane.showInputDialog(parentComp, message, title,
                JOptionPane.PLAIN_MESSAGE, null, null, current);
        if (val == null) return null; // cancelado
        val = val.trim();
        if (val.isEmpty()) {
            JOptionPane.showMessageDialog(parentComp,
                    "El valor no puede estar vacío.", "Valor inválido", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return val;
    }

    /** Verifica si el modelo contiene el valor (ignorando un índice). */
    private boolean containsValue(DefaultListModel<String> model, String value, int ignoreIdx) {
        for (int i = 0; i < model.getSize(); i++) {
            if (i == ignoreIdx) continue;
            if (model.getElementAt(i).equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Guarda la configuración tomando los valores del diálogo. Puede cerrar el
     * diálogo si {@code closeAfter} es true.
     *
     * @return true si se guardó correctamente, false si hubo errores.
     */
    private boolean saveConfig(ConfigDialog dialog) {
        // Validar campos de comentarios antes de continuar
        if (!validateComments(dialog)) {
            return false;
        }

        Config tmp = new Config();
        try {
            applyDialogToConfig(dialog, tmp);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(dialog, ex.getMessage(),
                    "Configuración inválida", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Previsualización JSON y confirmación
        Gson g = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JTextArea area = new JTextArea(g.toJson(tmp));
        area.setEditable(false);
        area.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(400, 300));
        int opt = JOptionPane.showConfirmDialog(dialog, sp,
                "Previsualización JSON", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) {
            return false; // usuario canceló
        }

        // Persistir
        try {
            configSaver.save(configPath, tmp);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(dialog, ex.getMessage(),
                    "Configuración inválida", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog,
                    "No se pudo guardar la configuración: " + e.getMessage(),
                    "Error de guardado", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Verificar guardado
        try {
            Config reloaded = configLoader.load(configPath);
            if (!g.toJson(reloaded).equals(g.toJson(tmp))) {
                JOptionPane.showMessageDialog(dialog,
                        "Error al verificar el guardado: el contenido difiere del esperado.",
                        "Error de guardado", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            copyInto(config, reloaded);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog,
                    "No se pudo verificar la configuración guardada: " + e.getMessage(),
                    "Error de guardado", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (onConfigChanged != null) {
            try {
                onConfigChanged.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        JOptionPane.showMessageDialog(dialog,
                "Configuración guardada correctamente.",
                "Configuración", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /** Valida que los campos de comentarios no estén vacíos. */
    private boolean validateComments(ConfigDialog dialog) {
        String linea = safeTrim(dialog.getComentarioLinea());
        if (linea == null || linea.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "El marcador de comentario de línea no puede estar vacío.",
                    "Dato faltante", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String bloqueIni = safeTrim(dialog.getComentarioBloqueInicio());
        if (bloqueIni == null || bloqueIni.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "El marcador de inicio de comentario de bloque no puede estar vacío.",
                    "Dato faltante", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String bloqueFin = safeTrim(dialog.getComentarioBloqueFin());
        if (bloqueFin == null || bloqueFin.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "El marcador de fin de comentario de bloque no puede estar vacío.",
                    "Dato faltante", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private static void preloadDialog(ConfigDialog dialog, Config cfg) {
        dialog.setReservadas(new ArrayList<>(cfg.getPalabrasReservadas()));
        dialog.setOperadores(new ArrayList<>(cfg.getOperadores()));
        dialog.setPuntuacion(new ArrayList<>(cfg.getPuntuacion()));
        dialog.setAgrupacion(new ArrayList<>(cfg.getAgrupacion()));

        CommentsConfig cc = cfg.getComentarios();
        if (cc != null) {
            dialog.setComentarioLinea(cc.getLinea());
            dialog.setComentarioBloqueInicio(cc.getBloqueInicio());
            dialog.setComentarioBloqueFin(cc.getBloqueFin());
        }
    }

    private static void applyDialogToConfig(ConfigDialog dialog, Config cfg) {
        List<String> reservadas = normalizeList(dialog.getReservadas(), "palabras reservadas");
        List<String> operadores = normalizeList(dialog.getOperadores(), "operadores");
        List<String> puntuacion = normalizeList(dialog.getPuntuacion(), "puntuación");
        List<String> agrupacion = normalizeList(dialog.getAgrupacion(), "agrupación");

        String linea = safeTrim(dialog.getComentarioLinea());
        String bloqueIni = safeTrim(dialog.getComentarioBloqueInicio());
        String bloqueFin = safeTrim(dialog.getComentarioBloqueFin());

        // Validaciones mínimas exigidas por la práctica
        if (linea == null || linea.isEmpty()) {
            throw new IllegalArgumentException("El marcador de comentario de línea no puede estar vacío.");
        }
        if (bloqueIni == null || bloqueIni.isEmpty()) {
            throw new IllegalArgumentException("El marcador de inicio de comentario de bloque no puede estar vacío.");
        }
        if (bloqueFin == null || bloqueFin.isEmpty()) {
            throw new IllegalArgumentException("El marcador de fin de comentario de bloque no puede estar vacío.");
        }

        // Aplicar al modelo vivo
        cfg.setPalabrasReservadas(new LinkedHashSet<>(reservadas));
        cfg.setOperadores(new LinkedHashSet<>(operadores));
        cfg.setPuntuacion(new LinkedHashSet<>(puntuacion));
        cfg.setAgrupacion(new LinkedHashSet<>(agrupacion));

        CommentsConfig cc = cfg.getComentarios();
        if (cc == null)
            cc = new CommentsConfig();
        cc.setLinea(linea);
        cc.setBloqueInicio(bloqueIni);
        cc.setBloqueFin(bloqueFin);
        cfg.setComentarios(cc);
    }

    private static List<String> normalizeList(List<String> in, String name) {
        if (in == null)
            return List.of();
        Set<String> set = new LinkedHashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        for (String s : in) {
            String v = safeTrim(s);
            if (v == null || v.isEmpty()) {
                throw new IllegalArgumentException("La lista '" + name + "' contiene valores vacíos.");
            }
            if (!set.add(v)) {
                duplicates.add(v);
            }
        }
        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException("La lista '" + name + "' tiene valores duplicados: " + duplicates);
        }
        return new ArrayList<>(set);
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    /** Copia profunda de los campos relevantes de {@code src} a {@code dst}. */
    private static void copyInto(Config dst, Config src) {
        if (src == null || dst == null)
            return;
        dst.setPalabrasReservadas(new LinkedHashSet<>(src.getPalabrasReservadas()));
        dst.setOperadores(new LinkedHashSet<>(src.getOperadores()));
        dst.setPuntuacion(new LinkedHashSet<>(src.getPuntuacion()));
        dst.setAgrupacion(new LinkedHashSet<>(src.getAgrupacion()));
        CommentsConfig sc = src.getComentarios();
        if (sc != null) {
            CommentsConfig cc = new CommentsConfig();
            cc.setLinea(sc.getLinea());
            cc.setBloqueInicio(sc.getBloqueInicio());
            cc.setBloqueFin(sc.getBloqueFin());
            dst.setComentarios(cc);
        }
    }
}
