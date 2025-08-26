package view.components;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Área de edición principal. Vista pasiva: no hace análisis ni búsqueda; expone
 * API para que el Controller y los servicios (Highlight/Search/File/Analyze)
 * operen sobre el documento.
 */
public class EditorPanel extends javax.swing.JPanel {

    /*==================== Handlers (inyectados por el Controller) ====================*/
    private Runnable onFind;    // Ctrl+F
    private Runnable onSave;    // Ctrl+S
    private Runnable onAnalyze; // F5

    /**
     * Creates new form EditorPanel
     */
    public EditorPanel() {
        initComponents();
        postInitConfigure();
    }

    /**
     * Ajustes de UI posteriores al initComponents()
     */
    private void postInitConfigure() {
        // Fuente monoespaciada ya definida en el diseñador; usar documento estilado
        txtEditor.setEditorKit(new StyledEditorKit());
        txtEditor.setDocument(new DefaultStyledDocument());
        txtEditor.setCaretPosition(0);
        txtEditor.setDragEnabled(true);
        scrollEditor.setWheelScrollingEnabled(true);

        // Instalar atajos estándar (delegan en handlers inyectados)
        installKeyBindings();
    }

    private void installKeyBindings() {
        // Keymap global del panel (cuando cualquier hijo tiene el foco)
        InputMap im = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = this.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "find");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "analyze");

        am.put("find", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onFind != null) {
                    onFind.run();
                }
            }
        });
        am.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onSave != null) {
                    onSave.run();
                }
            }
        });
        am.put("analyze", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onAnalyze != null) {
                    onAnalyze.run();
                }
            }
        });
    }

    /* ============================== API de Vista ============================== */
    /**
     * Devuelve el JTextPane del editor (para binds finos, si hiciera falta).
     */
    public JTextPane getEditorPane() {
        return txtEditor;
    }

    /**
     * Devuelve el Document del editor.
     */
    public Document getDocument() {
        return txtEditor.getDocument();
    }

    /**
     * Devuelve el StyledDocument del editor (para coloreo por atributos).
     */
    public StyledDocument getStyledDocument() {
        return (StyledDocument) txtEditor.getDocument();
    }

    /**
     * Devuelve el Highlighter del editor (por si se prefiere destacar rangos).
     */
    public Highlighter getHighlighter() {
        return txtEditor.getHighlighter();
    }

    /**
     * Obtiene el texto completo del editor.
     */
    public String getEditorText() {
        return txtEditor.getText();
    }

    /**
     * Reemplaza el texto del editor (caret al inicio).
     */
    public void setEditorText(String text) {
        txtEditor.setText(text == null ? "" : text);
        txtEditor.setCaretPosition(0);
    }

    /**
     * Solicita foco en el editor y selecciona todo (útil tras abrir archivo).
     */
    public void focusEditorSelectAll() {
        txtEditor.requestFocusInWindow();
        txtEditor.selectAll();
    }

    /**
     * Selecciona el rango [start, end) y lo hace visible.
     */
    public void selectRange(int startOffset, int endOffset) {
        int len = getDocument().getLength();
        int s = Math.max(0, Math.min(startOffset, len));
        int e = Math.max(s, Math.min(endOffset, len));
        txtEditor.select(s, e);
        ensureOffsetVisible(s, e);
    }

    /**
     * Desplaza el viewport para hacer visible el rango solicitado y coloca el
     * caret al inicio.
     */
    public void ensureOffsetVisible(int start, int end) {
        try {
            Rectangle r1 = txtEditor.modelToView(start);
            Rectangle r2 = txtEditor.modelToView(Math.max(start, Math.min(end, getDocument().getLength())));
            if (r1 != null && r2 != null) {
                Rectangle union = r1.union(r2);
                txtEditor.scrollRectToVisible(union);
                txtEditor.setCaretPosition(start);
            }
        } catch (BadLocationException ignored) {
        }
    }

    /**
     * Limpia todos los resaltados actuales (si se usó Highlighter).
     */
    public void clearHighlights() {
        txtEditor.getHighlighter().removeAllHighlights();
    }

    /**
     * Aplica atributos (color/estilo) en un rango del documento (coloreo
     * léxico). El controlador/servicio debe suministrar el AttributeSet.
     */
    public void applyAttributes(int start, int length, AttributeSet attrs) {
        if (length <= 0) {
            return;
        }
        try {
            getStyledDocument().setCharacterAttributes(start, length, attrs, true);
        } catch (Exception ignored) {
        }
    }

    /**
     * Restaura atributos por defecto en todo el documento (elimina coloreo).
     */
    public void resetAttributes() {
        try {
            SimpleAttributeSet def = new SimpleAttributeSet();
            getStyledDocument().setCharacterAttributes(0, getDocument().getLength(), def, true);
        } catch (Exception ignored) {
        }
    }

    /**
     * Actualiza la barra de estado (posiciones) con texto formateado desde el
     * Controller.
     */
    public void setStatusPositionText(String text) {
        lblPos.setText(text == null ? "" : text);
    }

    /**
     * Actualiza la barra de estado (estadísticas) con texto formateado desde el
     * Controller.
     */
    public void setStatusStatsText(String text) {
        lblStats.setText(text == null ? "" : text);
    }

    /**
     * Bindings de handlers externos (Controller).
     */
    public void setOnFind(Runnable onFind) {
        this.onFind = onFind;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    public void setOnAnalyze(Runnable onAnalyze) {
        this.onAnalyze = onAnalyze;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelEstado = new javax.swing.JPanel();
        lblPos = new javax.swing.JLabel();
        lblStats = new javax.swing.JLabel();
        scrollEditor = new javax.swing.JScrollPane();
        txtEditor = new javax.swing.JTextPane();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setLayout(new java.awt.BorderLayout());

        panelEstado.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 2, 4));
        panelEstado.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblPos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblPos.setText("Línea: – Columna: –");
        panelEstado.add(lblPos);

        lblStats.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblStats.setText("Caracteres: –");
        panelEstado.add(lblStats);

        add(panelEstado, java.awt.BorderLayout.PAGE_END);

        txtEditor.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        txtEditor.setMargin(new java.awt.Insets(6, 8, 6, 8));
        scrollEditor.setViewportView(txtEditor);

        add(scrollEditor, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblPos;
    private javax.swing.JLabel lblStats;
    private javax.swing.JPanel panelEstado;
    private javax.swing.JScrollPane scrollEditor;
    private javax.swing.JTextPane txtEditor;
    // End of variables declaration//GEN-END:variables
}
