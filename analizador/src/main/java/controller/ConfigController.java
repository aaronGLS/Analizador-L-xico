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

    /** Abre el diálogo de configuración y gestiona guardado/validación. */
    public void openDialogAndSave() {
        // Instanciar y precargar el diálogo
        Frame frameParent = JOptionPane.getFrameForComponent(parent);
        ConfigDialog dialog = new ConfigDialog(frameParent, true);
        preloadDialog(dialog, config);

        // Listeners para manipular listas con validación de duplicados
        dialog.setOnAddReservada(() -> addItem(dialog.getLstReservadas(), "palabra reservada"));
        dialog.setOnEditReservada(() -> editItem(dialog.getLstReservadas(), "palabra reservada"));
        dialog.setOnAddOperador(() -> addItem(dialog.getLstOperadores(), "operador"));
        dialog.setOnEditOperador(() -> editItem(dialog.getLstOperadores(), "operador"));
        dialog.setOnAddPuntuacion(() -> addItem(dialog.getLstPuntuacion(), "signo de puntuación"));
        dialog.setOnEditPuntuacion(() -> editItem(dialog.getLstPuntuacion(), "signo de puntuación"));
        dialog.setOnAddAgrupacion(() -> addItem(dialog.getLstAgrupacion(), "símbolo de agrupación"));
        dialog.setOnEditAgrupacion(() -> editItem(dialog.getLstAgrupacion(), "símbolo de agrupación"));

        // Botones inferiores
        dialog.getBtnCancelar().addActionListener(e -> dialog.dispose());
        dialog.setOnAplicar(() -> saveConfig(dialog));
        dialog.setOnAceptar(() -> {
            if (saveConfig(dialog)) {
                dialog.dispose();
            }
        });

        // Mostrar de forma modal
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /** Guarda la configuración actual del diálogo mostrando previsualización JSON. */
    private boolean saveConfig(ConfigDialog dialog) {
        Config temp = new Config();
        try {
            applyDialogToConfig(dialog, temp);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(),
                    "Configuración inválida", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String json = gson.toJson(temp);
        JTextArea area = new JTextArea(json, 20, 60);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        int option = JOptionPane.showConfirmDialog(dialog, new JScrollPane(area),
                "Previsualización JSON", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return false;
        }

        dialog.setEnabledWhileSaving(false);
        try {
            configSaver.save(configPath, temp);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "No se pudo guardar la configuración: " + e.getMessage(),
                    "Error de guardado", JOptionPane.ERROR_MESSAGE);
            dialog.setEnabledWhileSaving(true);
            return false;
        }

        try {
            Config reloaded = configLoader.load(configPath);
            copyInto(config, reloaded);
        } catch (Exception ignore) {
        }
        dialog.setEnabledWhileSaving(true);

        if (onConfigChanged != null) {
            try {
                onConfigChanged.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        JOptionPane.showMessageDialog(parent,
                "Configuración guardada correctamente.",
                "Configuración", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    // ==== Helpers de edición de listas ====
    private void addItem(JList<String> list, String nombre) {
        DefaultListModel<String> model = castModel(list);
        String val = JOptionPane.showInputDialog(parent, "Ingrese " + nombre + ":");
        if (val == null) {
            return; // cancelado
        }
        val = val.trim();
        if (val.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "El valor no puede estar vacío.",
                    "Entrada inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (modelContains(model, val, -1)) {
            JOptionPane.showMessageDialog(parent, "Valor duplicado: " + val,
                    "Entrada duplicada", JOptionPane.ERROR_MESSAGE);
            return;
        }
        model.addElement(val);
        list.setSelectedIndex(model.getSize() - 1);
    }

    private void editItem(JList<String> list, String nombre) {
        DefaultListModel<String> model = castModel(list);
        int idx = list.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        String actual = model.get(idx);
        String val = JOptionPane.showInputDialog(parent, "Editar " + nombre + ":", actual);
        if (val == null) {
            return;
        }
        val = val.trim();
        if (val.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "El valor no puede estar vacío.",
                    "Entrada inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (modelContains(model, val, idx)) {
            JOptionPane.showMessageDialog(parent, "Valor duplicado: " + val,
                    "Entrada duplicada", JOptionPane.ERROR_MESSAGE);
            return;
        }
        model.set(idx, val);
        list.setSelectedIndex(idx);
    }

    @SuppressWarnings("unchecked")
    private DefaultListModel<String> castModel(JList<String> list) {
        return (DefaultListModel<String>) list.getModel();
    }

    private boolean modelContains(DefaultListModel<String> model, String val, int ignoreIndex) {
        for (int i = 0; i < model.size(); i++) {
            if (i == ignoreIndex) {
                continue;
            }
            if (model.get(i).equals(val)) {
                return true;
            }
        }
        return false;
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
