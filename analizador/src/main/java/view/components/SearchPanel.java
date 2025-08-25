package view.components;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Panel de Búsqueda ("nueva área"), vista pasiva. Muestra el texto espejo (solo
 * lectura) y expone controles para que el Controller ejecute la búsqueda char a
 * char, resalte coincidencias y navegue entre ellas. \n * No contiene lógica de
 * búsqueda; solo configuración de UI y API para el Controller.
 */
public class SearchPanel extends javax.swing.JPanel {

    /*==================== Handlers (inyectados por el Controller) ====================*/
    private Runnable onSearch;   // Buscar (Enter / botón)
    private Runnable onNext;     // Siguiente (F3 / Enter si ya buscó)
    private Runnable onPrev;     // Anterior (Shift+F3 / Shift+Enter)
    private Runnable onClose;    // Cerrar (Esc)

    /**
     * Creates new form SearchPanel
     */
    public SearchPanel() {
        initComponents();
        postInitConfigure();
    }

    /**
     * Ajustes de UI posteriores al initComponents()
     */
    private void postInitConfigure() {
        // 1) Layout: que el campo de búsqueda se expanda en GridBagLayout
        if (panelControles.getLayout() instanceof java.awt.GridBagLayout gbl) {
            var comps = panelControles.getComponents();
            for (Component c : comps) {
                var gbc = gbl.getConstraints(c);
                gbc.insets = new Insets(2, 4, 2, 4);
                if (c == txtQuery) {
                    gbc.weightx = 1.0; // se expande horizontalmente
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                } else {
                    gbc.weightx = 0.0;
                }
                gbl.setConstraints(c, gbc);
            }
        }

        // 2) Campo de búsqueda
        txtQuery.setColumns(32);
        txtQuery.setText(""); // quitar el placeholder por defecto
        txtQuery.putClientProperty("JComponent.sizeVariant", "regular");
        txtQuery.setFocusAccelerator('F'); // Alt+F centra el foco en el campo

        // 3) Botones y checks: mnemonics para accesibilidad
        btnBuscar.setMnemonic('B');
        btnAnterior.setMnemonic('A');
        btnSiguiente.setMnemonic('S');
        chkIgnorarMayus.setMnemonic('I');
        chkPalabraCompleta.setMnemonic('P');

        // 4) JTextPane de vista previa: solo lectura y contenido de texto plano
        txtPreview.setEditable(false);
        try {
            txtPreview.setContentType("text/plain");
        } catch (Exception ignored) {
            /* JTextPane lo soporta vía JEditorPane */ }
        txtPreview.setCaretPosition(0);

        // 5) KeyBindings (no lógica): mapeamos teclas a handlers inyectados
        installKeyBindings();
    }

    private void installKeyBindings() {
        // Buscar (Enter) cuando el foco está en txtQuery
        var imQuery = txtQuery.getInputMap(JComponent.WHEN_FOCUSED);
        var amQuery = txtQuery.getActionMap();
        imQuery.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search");
        amQuery.put("search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onSearch != null) {
                    onSearch.run();
                }
            }
        });

        // Navegación global sobre el panel: F3 siguiente, Shift+F3 anterior, Esc cerrar
        var imPanel = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        var amPanel = this.getActionMap();

        imPanel.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "next");
        imPanel.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "next");
        imPanel.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), "prev");
        imPanel.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "prev");
        imPanel.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");

        amPanel.put("next", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onNext != null) {
                    onNext.run();
                }
            }
        });
        amPanel.put("prev", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onPrev != null) {
                    onPrev.run();
                }
            }
        });
        amPanel.put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onClose != null) {
                    onClose.run();
                }
            }
        });

        // Clicks en botones → delegan a los handlers
        btnBuscar.addActionListener(e -> {
            if (onSearch != null) {
                onSearch.run();
            }
        });
        btnSiguiente.addActionListener(e -> {
            if (onNext != null) {
                onNext.run();
            }
        });
        btnAnterior.addActionListener(e -> {
            if (onPrev != null) {
                onPrev.run();
            }
        });
    }

    /*============================== API de Vista ==============================*/
    /**
     * Asigna el texto espejo del editor en la vista previa.
     */
    public void setPreviewText(String text) {
        txtPreview.setText(text == null ? "" : text);
        txtPreview.setCaretPosition(0);
    }

    /**
     * Devuelve el documento de la vista previa por si el Controller quiere
     * manipularlo directamente.
     */
    public javax.swing.text.Document getPreviewDocument() {
        return txtPreview.getDocument();
    }

    /**
     * Limpia todos los resaltados actuales.
     */
    public void clearHighlights() {
        txtPreview.getHighlighter().removeAllHighlights();
    }

    /**
     * Añade un resaltado en el rango [start, end) usando el painter entregado
     * por el Controller.
     */
    public Object addHighlight(int start, int end, Highlighter.HighlightPainter painter) {
        try {
            return txtPreview.getHighlighter().addHighlight(start, end, painter);
        } catch (BadLocationException e) {
            return null;
        }
    }

    /**
     * Elimina un resaltado previamente agregado (usa el objeto devuelto por
     * addHighlight).
     */
    public void removeHighlight(Object tag) {
        txtPreview.getHighlighter().removeHighlight(tag);
    }

    /**
     * Desplaza la vista para hacer visible el rango solicitado y coloca el
     * caret al inicio.
     */
    public void ensureRangeVisible(int start, int end) {
        try {
            Rectangle r1 = txtPreview.modelToView(start);
            Rectangle r2 = txtPreview.modelToView(Math.max(start, Math.min(end, txtPreview.getDocument().getLength())));
            if (r1 != null && r2 != null) {
                Rectangle union = r1.union(r2);
                txtPreview.scrollRectToVisible(union);
                txtPreview.setCaretPosition(start);
            }
        } catch (BadLocationException ignored) {
        }
    }

    /**
     * Enfoca el campo de búsqueda y selecciona su contenido.
     */
    public void focusQuery() {
        txtQuery.requestFocusInWindow();
        txtQuery.selectAll();
    }

    /**
     * Getters/Setters de controles (para Controller)
     */
    public String getQueryText() {
        return txtQuery.getText();
    }

    public void setQueryText(String s) {
        txtQuery.setText(s == null ? "" : s);
    }

    public boolean isIgnoreCaseSelected() {
        return chkIgnorarMayus.isSelected();
    }

    public void setIgnoreCaseSelected(boolean v) {
        chkIgnorarMayus.setSelected(v);
    }

    public boolean isWholeWordSelected() {
        return chkPalabraCompleta.isSelected();
    }

    public void setWholeWordSelected(boolean v) {
        chkPalabraCompleta.setSelected(v);
    }

    /**
     * Actualiza el contador como "Coincidencias: N"
     */
    public void setMatchesCount(int total) {
        lblContador.setText("Coincidencias: " + total);
    }

    /**
     * Actualiza el contador como "Coincidencias: i / N" (1-indexado
     * recomendado).
     */
    public void setMatchPosition(int indexOneBased, int total) {
        lblContador.setText("Coincidencias: " + indexOneBased + " / " + total);
    }

    /**
     * Exposición directa de componentes por si el Controller quiere más
     * control.
     */
    public JTextPane getPreviewPane() {
        return txtPreview;
    }

    public JTextField getQueryField() {
        return txtQuery;
    }

    public JCheckBox getIgnoreCaseCheck() {
        return chkIgnorarMayus;
    }

    public JCheckBox getWholeWordCheck() {
        return chkPalabraCompleta;
    }

    /**
     * Bindings de handlers externos (Controller).
     */
    public void setOnSearch(Runnable onSearch) {
        this.onSearch = onSearch;
    }

    public void setOnNext(Runnable onNext) {
        this.onNext = onNext;
    }

    public void setOnPrev(Runnable onPrev) {
        this.onPrev = onPrev;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    /* ======================= API adicional para SearchController simple ======================= */
    /**
     * Renderiza el texto espejo y resalta los rangos entregados.
     * Si la lista es null o vacía, solo muestra el texto y limpia resaltados.
     */
    public void render(String text, java.util.List<model.search.MatchRange> ranges) {
        setPreviewText(text);
        clearHighlights();
        if (ranges == null || ranges.isEmpty()) {
            setMatchesCount(0);
            return;
        }
        Highlighter.HighlightPainter painter = new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(new Color(255, 255, 0));
        for (model.search.MatchRange r : ranges) {
            int start = r.startIndex();
            int endExclusive = r.endIndex() + 1; // modelo usa end inclusive
            addHighlight(start, endExclusive, painter);
        }
        setMatchesCount(ranges.size());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelControles = new javax.swing.JPanel();
        lblBuscar = new javax.swing.JLabel();
        txtQuery = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        chkIgnorarMayus = new javax.swing.JCheckBox();
        chkPalabraCompleta = new javax.swing.JCheckBox();
        btnAnterior = new javax.swing.JButton();
        btnSiguiente = new javax.swing.JButton();
        lblContador = new javax.swing.JLabel();
        scrollPreview = new javax.swing.JScrollPane();
        txtPreview = new javax.swing.JTextPane();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setLayout(new java.awt.BorderLayout());

        panelControles.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panelControles.setLayout(new java.awt.GridBagLayout());

        lblBuscar.setText("Buscar:");
        panelControles.add(lblBuscar, new java.awt.GridBagConstraints());

        txtQuery.setText("jTextField1");
        txtQuery.setToolTipText("Escribe el término a buscar");
        panelControles.add(txtQuery, new java.awt.GridBagConstraints());

        btnBuscar.setText("Buscar");
        btnBuscar.setToolTipText("Buscar todas las coincidencias (Enter)");
        panelControles.add(btnBuscar, new java.awt.GridBagConstraints());

        chkIgnorarMayus.setText("Ignorar mayúsculas");
        panelControles.add(chkIgnorarMayus, new java.awt.GridBagConstraints());

        chkPalabraCompleta.setText("Palabra completa");
        panelControles.add(chkPalabraCompleta, new java.awt.GridBagConstraints());

        btnAnterior.setText("Anterior");
        btnAnterior.setToolTipText("Coincidencia anterior ( Shift+Enter)");
        panelControles.add(btnAnterior, new java.awt.GridBagConstraints());

        btnSiguiente.setText("Siguiente");
        btnSiguiente.setToolTipText("Coincidencia siguiente (F3)");
        panelControles.add(btnSiguiente, new java.awt.GridBagConstraints());

        lblContador.setText("Coincidencias: –");
        panelControles.add(lblContador, new java.awt.GridBagConstraints());

        add(panelControles, java.awt.BorderLayout.PAGE_START);

        txtPreview.setEditable(false);
        txtPreview.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtPreview.setToolTipText("Vista de solo lectura; las coincidencias aparecen resaltadas");
        txtPreview.setMargin(new java.awt.Insets(6, 8, 6, 8));
        scrollPreview.setViewportView(txtPreview);

        add(scrollPreview, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnterior;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnSiguiente;
    private javax.swing.JCheckBox chkIgnorarMayus;
    private javax.swing.JCheckBox chkPalabraCompleta;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblContador;
    private javax.swing.JPanel panelControles;
    private javax.swing.JScrollPane scrollPreview;
    private javax.swing.JTextPane txtPreview;
    private javax.swing.JTextField txtQuery;
    // End of variables declaration//GEN-END:variables
}
