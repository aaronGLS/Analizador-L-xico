package view;

import view.components.EditorPanel;
import view.components.SearchPanel;
import view.components.ReportsPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Ventana principal. Estructura (sin lógica): - Arriba: Editor (EditorPanel) +
 * Búsqueda colapsable (SearchPanel) en un JSplitPane vertical. - Abajo:
 * Reportes (ReportsPanel) en un JSplitPane vertical principal. - Barra de menús
 * con acciones. Esta vista es PASIVA; el Controller conecta toda la lógica.
 */
public class MainWindow extends javax.swing.JFrame {

    // Subvistas reales embebidas en los placeholders
    private EditorPanel editorPanelView;
    private SearchPanel searchPanelView;
    private ReportsPanel reportsPanelView;

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        postInitConfigure();
    }

    /**
     * Configuración posterior al initComponents(): - Monta las clases reales
     * (EditorPanel/SearchPanel/ReportsPanel) dentro de los placeholders. -
     * Ajusta dividers, mnemonics y aceleradores de menús. - Deja el SearchPanel
     * colapsado y los Reportes visibles por defecto.
     */
    private void postInitConfigure() {
        setLocationRelativeTo(null); // centrar ventana

        // Suavidad al arrastrar divisores
        splitMain.setContinuousLayout(true);
        splitEditorSearch.setContinuousLayout(true);

        // 1) Montaje de subvistas reales dentro de los placeholders
        panelEditor.setLayout(new BorderLayout());
        panelSearch.setLayout(new BorderLayout());
        panelReports.setLayout(new BorderLayout());

        editorPanelView = new EditorPanel();
        searchPanelView = new SearchPanel();
        reportsPanelView = new ReportsPanel();

        // Búsqueda: que pueda colapsar completamente y tenga un alto preferido al mostrarse
        searchPanelView.setMinimumSize(new Dimension(0, 0));
        searchPanelView.setPreferredSize(new Dimension(10, 180));

        panelEditor.removeAll();
        panelEditor.add(editorPanelView, BorderLayout.CENTER);

        panelSearch.removeAll();
        panelSearch.add(searchPanelView, BorderLayout.CENTER);

        panelReports.removeAll();
        panelReports.add(reportsPanelView, BorderLayout.CENTER);

        // 2) Dividers: Search colapsado (0 px) y Reports visibles (según resizeWeight del split principal)
        splitEditorSearch.setDividerLocation(0); // oculto al inicio
        SwingUtilities.invokeLater(() -> {
            // Asegurar proporción inicial de splitMain tras realizar el layout
            splitMain.setDividerLocation(0.65); // coincide con el resizeWeight configurado
        });

        // 3) Menús: mnemonics y aceleradores estándar (vista pasiva; el Controller añade los listeners)
        installMenuMnemonics();
        installMenuAccelerators();

        // Refrescar la jerarquía tras insertar las subvistas
        panelEditor.revalidate();
        panelEditor.repaint();
        panelSearch.revalidate();
        panelSearch.repaint();
        panelReports.revalidate();
        panelReports.repaint();
    }

    private void installMenuMnemonics() {
        mnuArchivo.setMnemonic('A');
        mnuEdicion.setMnemonic('E');
        mnuVer.setMnemonic('V');
        mnuAnalisis.setMnemonic('Z'); // evita colisiones comunes
        mnuConfig.setMnemonic('O');   // cOnfiguración
        mnuAyuda.setMnemonic('Y');
    }

    private void installMenuAccelerators() {
        miNuevo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        miAbrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        miGuardar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        miGuardarComo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        miExportarReportes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));

        miBuscar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        miBuscarSiguiente.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        miBuscarAnterior.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK));
        miLimpiarResaltados.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));

        miToggleSearch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        miToggleReports.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));

        miAnalizar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        miLimpiarResultados.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK));

        miConfigurarLenguaje.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.CTRL_DOWN_MASK));
        miAcercaDe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
    }

    /* ========================= API PASIVA PARA EL CONTROLLER ========================= */
    // --- Acceso a las subvistas ---
    public EditorPanel getEditorPanel() {
        return editorPanelView;
    }

    public SearchPanel getSearchPanel() {
        return searchPanelView;
    }

    public ReportsPanel getReportsPanel() {
        return reportsPanelView;
    }

    // --- Acceso a split panes (por si el Controller quiere animaciones personalizadas) ---
    public JSplitPane getSplitMain() {
        return splitMain;
    }

    public JSplitPane getSplitEditorSearch() {
        return splitEditorSearch;
    }

    // --- Mostrar / ocultar panel de búsqueda ---
    public void showSearchPanel() {
        int total = splitEditorSearch.getHeight();
        if (total <= 0) {
            SwingUtilities.invokeLater(this::showSearchPanel);
            return;
        }
        int pref = Math.max(140, searchPanelView.getPreferredSize().height);
        int loc = Math.max(80, total - pref);
        splitEditorSearch.setDividerLocation(loc);
        searchPanelView.getQueryField().requestFocusInWindow();
    }

    public void hideSearchPanel() {
        splitEditorSearch.setDividerLocation(0);
        editorPanelView.getEditorPane().requestFocusInWindow();
    }

    public void toggleSearchPanel() {
        if (isSearchPanelVisible()) {
            hideSearchPanel();
        } else {
            showSearchPanel();
        }
    }

    public boolean isSearchPanelVisible() {
        // Si el divider está en 0, asumimos colapsado
        return splitEditorSearch.getDividerLocation() > 0;
    }

    // --- Mostrar / ocultar reportes ---
    public void showReports() {
        splitMain.setDividerLocation(0.65); // proporción cómoda por defecto
    }

    public void hideReports() {
        // Mueve el divisor casi al final para "ocultar" la zona inferior
        splitMain.setDividerLocation(1.0);
    }

    public void toggleReports() {
        if (areReportsVisible()) {
            hideReports();
        } else {
            showReports();
        }
    }

    public boolean areReportsVisible() {
        // Consideramos oculto si el divisor está en la posición máxima
        int max = splitMain.getMaximumDividerLocation();
        return splitMain.getDividerLocation() < max;
    }

    /* ================= Getters expuestos para MainController ================= */
    public javax.swing.JMenuItem getMiNuevo() { return miNuevo; }
    public javax.swing.JMenuItem getMiAbrir() { return miAbrir; }
    public javax.swing.JMenuItem getMiGuardar() { return miGuardar; }
    public javax.swing.JMenuItem getMiGuardarComo() { return miGuardarComo; }
    public javax.swing.JMenuItem getMiExportarReportes() { return miExportarReportes; }
    public javax.swing.JMenuItem getMiBuscar() { return miBuscar; }
    public javax.swing.JMenuItem getMiBuscarSiguiente() { return miBuscarSiguiente; }
    public javax.swing.JMenuItem getMiBuscarAnterior() { return miBuscarAnterior; }
    public javax.swing.JMenuItem getMiLimpiarResaltados() { return miLimpiarResaltados; }
    public javax.swing.JMenuItem getMiToggleSearch() { return miToggleSearch; }
    public javax.swing.JMenuItem getMiToggleReports() { return miToggleReports; }
    public javax.swing.JMenuItem getMiAnalizar() { return miAnalizar; }
    public javax.swing.JMenuItem getMiLimpiarResultados() { return miLimpiarResultados; }
    public javax.swing.JMenuItem getMiConfigurarLenguaje() { return miConfigurarLenguaje; }
    public javax.swing.JMenuItem getMiAcercaDe() { return miAcercaDe; }

    /**
     * Actualiza el título de la ventana (útil para indicar archivo y estado
     * dirty).
     */
    public void updateWindowTitle(String base, String filePath, boolean dirty) {
        String mark = dirty ? " *" : "";
        if (filePath == null || filePath.isEmpty()) {
            setTitle(base + mark);
        } else {
            setTitle(base + " — " + filePath + mark);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitMain = new javax.swing.JSplitPane();
        panelTop = new javax.swing.JPanel();
        splitEditorSearch = new javax.swing.JSplitPane();
        panelEditor = new javax.swing.JPanel();
        panelSearch = new javax.swing.JPanel();
        panelReports = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        mnuArchivo = new javax.swing.JMenu();
        miNuevo = new javax.swing.JMenuItem();
        miAbrir = new javax.swing.JMenuItem();
        miGuardar = new javax.swing.JMenuItem();
        miGuardarComo = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        miExportarReportes = new javax.swing.JMenuItem();
        mnuEdicion = new javax.swing.JMenu();
        miBuscar = new javax.swing.JMenuItem();
        miBuscarSiguiente = new javax.swing.JMenuItem();
        miBuscarAnterior = new javax.swing.JMenuItem();
        miLimpiarResaltados = new javax.swing.JMenuItem();
        mnuVer = new javax.swing.JMenu();
        miToggleSearch = new javax.swing.JMenuItem();
        miToggleReports = new javax.swing.JMenuItem();
        mnuAnalisis = new javax.swing.JMenu();
        miAnalizar = new javax.swing.JMenuItem();
        miLimpiarResultados = new javax.swing.JMenuItem();
        mnuConfig = new javax.swing.JMenu();
        miConfigurarLenguaje = new javax.swing.JMenuItem();
        mnuAyuda = new javax.swing.JMenu();
        miAcercaDe = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Analizador léxico — práctica 1");
        setPreferredSize(new java.awt.Dimension(1100, 720));

        splitMain.setDividerSize(8);
        splitMain.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitMain.setResizeWeight(0.65);
        splitMain.setOneTouchExpandable(true);

        panelTop.setLayout(new java.awt.BorderLayout());

        splitEditorSearch.setDividerLocation(0);
        splitEditorSearch.setDividerSize(8);
        splitEditorSearch.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitEditorSearch.setResizeWeight(1.0);
        splitEditorSearch.setOneTouchExpandable(true);

        javax.swing.GroupLayout panelEditorLayout = new javax.swing.GroupLayout(panelEditor);
        panelEditor.setLayout(panelEditorLayout);
        panelEditorLayout.setHorizontalGroup(
            panelEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        panelEditorLayout.setVerticalGroup(
            panelEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        splitEditorSearch.setLeftComponent(panelEditor);

        javax.swing.GroupLayout panelSearchLayout = new javax.swing.GroupLayout(panelSearch);
        panelSearch.setLayout(panelSearchLayout);
        panelSearchLayout.setHorizontalGroup(
            panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        panelSearchLayout.setVerticalGroup(
            panelSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        splitEditorSearch.setBottomComponent(panelSearch);

        panelTop.add(splitEditorSearch, java.awt.BorderLayout.CENTER);

        splitMain.setTopComponent(panelTop);

        javax.swing.GroupLayout panelReportsLayout = new javax.swing.GroupLayout(panelReports);
        panelReports.setLayout(panelReportsLayout);
        panelReportsLayout.setHorizontalGroup(
            panelReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        panelReportsLayout.setVerticalGroup(
            panelReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 95, Short.MAX_VALUE)
        );

        splitMain.setRightComponent(panelReports);

        getContentPane().add(splitMain, java.awt.BorderLayout.CENTER);

        mnuArchivo.setText("Archivo");

        miNuevo.setText("Nuevo");
        mnuArchivo.add(miNuevo);

        miAbrir.setText("Abrir");
        mnuArchivo.add(miAbrir);

        miGuardar.setText("Guardar");
        mnuArchivo.add(miGuardar);

        miGuardarComo.setText("Guardar como");
        mnuArchivo.add(miGuardarComo);
        mnuArchivo.add(jSeparator1);

        miExportarReportes.setText("Exportar reportes");
        mnuArchivo.add(miExportarReportes);

        menuBar.add(mnuArchivo);

        mnuEdicion.setText("Edición");

        miBuscar.setText("Buscar");
        mnuEdicion.add(miBuscar);

        miBuscarSiguiente.setText("Siguiente");
        mnuEdicion.add(miBuscarSiguiente);

        miBuscarAnterior.setText("Anterior");
        mnuEdicion.add(miBuscarAnterior);

        miLimpiarResaltados.setText("Limpiar resultados");
        mnuEdicion.add(miLimpiarResaltados);

        menuBar.add(mnuEdicion);

        mnuVer.setText("Ver");

        miToggleSearch.setText("Mostrar/Ocultar panel de Búsqueda");
        mnuVer.add(miToggleSearch);

        miToggleReports.setText("Mostrar/Ocultar reportes");
        mnuVer.add(miToggleReports);

        menuBar.add(mnuVer);

        mnuAnalisis.setText("Análisis");

        miAnalizar.setText("Analizar");
        mnuAnalisis.add(miAnalizar);

        miLimpiarResultados.setText("Limpiar resultados");
        mnuAnalisis.add(miLimpiarResultados);

        menuBar.add(mnuAnalisis);

        mnuConfig.setText("Configuración");

        miConfigurarLenguaje.setText("Configurar lenguaje");
        mnuConfig.add(miConfigurarLenguaje);

        menuBar.add(mnuConfig);

        mnuAyuda.setText("Ayuda");

        miAcercaDe.setText("Acerca de");
        mnuAyuda.add(miAcercaDe);

        menuBar.add(mnuAyuda);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem miAbrir;
    private javax.swing.JMenuItem miAcercaDe;
    private javax.swing.JMenuItem miAnalizar;
    private javax.swing.JMenuItem miBuscar;
    private javax.swing.JMenuItem miBuscarAnterior;
    private javax.swing.JMenuItem miBuscarSiguiente;
    private javax.swing.JMenuItem miConfigurarLenguaje;
    private javax.swing.JMenuItem miExportarReportes;
    private javax.swing.JMenuItem miGuardar;
    private javax.swing.JMenuItem miGuardarComo;
    private javax.swing.JMenuItem miLimpiarResaltados;
    private javax.swing.JMenuItem miLimpiarResultados;
    private javax.swing.JMenuItem miNuevo;
    private javax.swing.JMenuItem miToggleReports;
    private javax.swing.JMenuItem miToggleSearch;
    private javax.swing.JMenu mnuAnalisis;
    private javax.swing.JMenu mnuArchivo;
    private javax.swing.JMenu mnuAyuda;
    private javax.swing.JMenu mnuConfig;
    private javax.swing.JMenu mnuEdicion;
    private javax.swing.JMenu mnuVer;
    private javax.swing.JPanel panelEditor;
    private javax.swing.JPanel panelReports;
    private javax.swing.JPanel panelSearch;
    private javax.swing.JPanel panelTop;
    private javax.swing.JSplitPane splitEditorSearch;
    private javax.swing.JSplitPane splitMain;
    // End of variables declaration//GEN-END:variables
}
