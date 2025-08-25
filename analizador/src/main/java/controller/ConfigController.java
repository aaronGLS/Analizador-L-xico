package controller;

import model.config.Config;
import model.config.CommentsConfig;
import view.dialogs.ConfigDialog;
import core.io.ConfigLoader;
import core.io.ConfigSaver;

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
        // Obtener Frame padre compatible con el constructor del diálogo
        Frame frameParent = JOptionPane.getFrameForComponent(parent);
        ConfigDialog dialog = new ConfigDialog(frameParent, true);
        preloadDialog(dialog, config);

        // Flag para saber si el usuario aceptó (OK) o canceló
        final boolean[] accepted = { false };
        dialog.getBtnAceptar().addActionListener(e -> {
            accepted[0] = true;
            dialog.dispose();
        });
        dialog.getBtnCancelar().addActionListener(e -> {
            accepted[0] = false;
            dialog.dispose();
        });

        // 2) Mostrar de forma modal y esperar acción del usuario
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        // Si el diálogo expone aceptación/cancelación, la consultamos.
        // Si no, asumimos que cerrar equivale a cancelar cuando no hay cambios.
        if (!accepted[0]) {
            return; // usuario canceló
        }

        // 3) Recoger datos, normalizar y validar
        try {
            applyDialogToConfig(dialog, config);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(),
                    "Configuración inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4) Persistir
        try {
            configSaver.save(configPath, config);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "No se pudo guardar la configuración: " + e.getMessage(),
                    "Error de guardado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 5) (Opcional) Recargar desde disco si tu implementación así lo requiere
        try {
            Config reloaded = configLoader.load(configPath);
            // Copiar de vuelta al objeto vivo para no romper referencias
            copyInto(config, reloaded);
        } catch (Exception ignore) {
            // Si tu implementación no necesita recargar, se puede omitir.
        }

        // 6) Notificar a la aplicación para reflejar cambios (resaltado / re-análisis)
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
        List<String> reservadas = normalizeList(dialog.getReservadas());
        List<String> operadores = normalizeList(dialog.getOperadores());
        List<String> puntuacion = normalizeList(dialog.getPuntuacion());
        List<String> agrupacion = normalizeList(dialog.getAgrupacion());

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

    private static List<String> normalizeList(List<String> in) {
        if (in == null)
            return List.of();
        Set<String> set = new LinkedHashSet<>(); // conserva orden e ignora duplicados
        for (String s : in) {
            String v = safeTrim(s);
            if (v != null && !v.isEmpty())
                set.add(v);
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
