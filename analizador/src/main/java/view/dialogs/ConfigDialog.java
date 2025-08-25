package view.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo de configuración (config.json) — Vista pasiva.
 *
 * Permite editar listas de Reservadas, Operadores, Puntuación, Agrupación, y
 * los delimitadores de Comentarios. No realiza validación ni I/O; el Controller
 * inyecta datos, valida, persiste y conecta las acciones.
 */
public class ConfigDialog extends javax.swing.JDialog {

    /* ======================== Handlers inyectables (Controller) ======================== */
    // Reservadas
    private Runnable onAddReservada, onEditReservada, onRemoveReservada, onUpReservada, onDownReservada;
    // Operadores
    private Runnable onAddOperador, onEditOperador, onRemoveOperador, onUpOperador, onDownOperador;
    // Puntuación
    private Runnable onAddPuntuacion, onEditPuntuacion, onRemovePuntuacion, onUpPuntuacion, onDownPuntuacion;
    // Agrupación
    private Runnable onAddAgrupacion, onEditAgrupacion, onRemoveAgrupacion, onUpAgrupacion, onDownAgrupacion;
    // Botonera inferior
    private Runnable onAplicar, onAceptar, onCancelar, onRestaurar;

    /**
     * Creates new form ConfigDialog
     */
    public ConfigDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        postInitConfigure();
    }

    /**
     * Ajustes de UX y wiring de acciones de la vista (sin lógica de negocio).
     */
    private void postInitConfigure() {
        setLocationRelativeTo(getParent());
        getRootPane().setDefaultButton(jButton2); // Aceptar como botón por defecto

        // Mnemonics en botones inferiores
        jButton1.setMnemonic('A'); // Aplicar
        jButton2.setMnemonic('O'); // Aceptar (OK)
        jButton3.setMnemonic('C'); // Cancelar
        jButton4.setMnemonic('R'); // Restaurar predeterminados

        // Modelos reales para las listas (en tiempo de diseño venías usando AbstractListModel "dummy")
        lstReservadas.setModel(new DefaultListModel<>());
        lstOperadores.setModel(new DefaultListModel<>());
        lstPuntuacion.setModel(new DefaultListModel<>());
        lstAgrupacion.setModel(new DefaultListModel<>());

        // Ajustes visuales uniformes para las listas
        configureList(lstReservadas);
        configureList(lstOperadores);
        configureList(lstPuntuacion);
        configureList(lstAgrupacion);

        // Doble clic = Editar en cada lista
        installDoubleClickToEdit(lstReservadas, () -> runSafely(onEditReservada));
        installDoubleClickToEdit(lstOperadores, () -> runSafely(onEditOperador));
        installDoubleClickToEdit(lstPuntuacion, () -> runSafely(onEditPuntuacion));
        installDoubleClickToEdit(lstAgrupacion, () -> runSafely(onEditAgrupacion));

        // KeyBindings por lista (Insert, Enter, Delete, Ctrl+↑/↓)
        installListKeyBindings(lstReservadas,
                () -> runSafely(onAddReservada),
                () -> runSafely(onEditReservada),
                () -> runSafely(onRemoveReservada),
                () -> runSafely(onUpReservada),
                () -> runSafely(onDownReservada));

        installListKeyBindings(lstOperadores,
                () -> runSafely(onAddOperador),
                () -> runSafely(onEditOperador),
                () -> runSafely(onRemoveOperador),
                () -> runSafely(onUpOperador),
                () -> runSafely(onDownOperador));

        installListKeyBindings(lstPuntuacion,
                () -> runSafely(onAddPuntuacion),
                () -> runSafely(onEditPuntuacion),
                () -> runSafely(onRemovePuntuacion),
                () -> runSafely(onUpPuntuacion),
                () -> runSafely(onDownPuntuacion));

        installListKeyBindings(lstAgrupacion,
                () -> runSafely(onAddAgrupacion),
                () -> runSafely(onEditAgrupacion),
                () -> runSafely(onRemoveAgrupacion),
                () -> runSafely(onUpAgrupacion),
                () -> runSafely(onDownAgrupacion));

        // Popup contextual con Acciones (Add/Edit/Remove/Up/Down) — sin modificar el diseñador
        installListPopup(lstReservadas,
                () -> runSafely(onAddReservada), () -> runSafely(onEditReservada), () -> runSafely(onRemoveReservada),
                () -> runSafely(onUpReservada), () -> runSafely(onDownReservada));
        installListPopup(lstOperadores,
                () -> runSafely(onAddOperador), () -> runSafely(onEditOperador), () -> runSafely(onRemoveOperador),
                () -> runSafely(onUpOperador), () -> runSafely(onDownOperador));
        installListPopup(lstPuntuacion,
                () -> runSafely(onAddPuntuacion), () -> runSafely(onEditPuntuacion), () -> runSafely(onRemovePuntuacion),
                () -> runSafely(onUpPuntuacion), () -> runSafely(onDownPuntuacion));
        installListPopup(lstAgrupacion,
                () -> runSafely(onAddAgrupacion), () -> runSafely(onEditAgrupacion), () -> runSafely(onRemoveAgrupacion),
                () -> runSafely(onUpAgrupacion), () -> runSafely(onDownAgrupacion));

        // Botonera inferior delega en handlers (el Controller inyecta la lógica)
        jButton1.addActionListener(e -> runSafely(onAplicar));
        jButton2.addActionListener(e -> runSafely(onAceptar));
        jButton3.addActionListener(e -> runSafely(onCancelar));
        jButton4.addActionListener(e -> runSafely(onRestaurar));
    }

    /* ============================== Utilidades de vista ============================== */
    private void configureList(JList<String> list) {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(-1);
        list.setFixedCellHeight(22);
        list.setPrototypeCellValue("xxxxxxxxxxxxxxxxxxxxxxxx");
    }

    private void installDoubleClickToEdit(JList<String> list, Runnable onEdit) {
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onEdit != null) {
                    onEdit.run();
                }
            }
        });
    }

    private void installListKeyBindings(JList<String> list,
            Runnable onAdd, Runnable onEdit, Runnable onRemove,
            Runnable onUp, Runnable onDown) {
        InputMap im = list.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = list.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "add");
        am.put("add", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onAdd != null) {
                    onAdd.run();
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "edit");
        am.put("edit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onEdit != null) {
                    onEdit.run();
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove");
        am.put("remove", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onRemove != null) {
                    onRemove.run();
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), "up");
        am.put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onUp != null) {
                    onUp.run();
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), "down");
        am.put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onDown != null) {
                    onDown.run();
                }
            }
        });
    }

    private void installListPopup(JList<String> list, Runnable onAdd, Runnable onEdit, Runnable onRemove,
            Runnable onUp, Runnable onDown) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miAdd = new JMenuItem("Agregar");
        miAdd.addActionListener(e -> {
            if (onAdd != null) {
                onAdd.run();
            }
        });
        JMenuItem miEdit = new JMenuItem("Editar");
        miEdit.addActionListener(e -> {
            if (onEdit != null) {
                onEdit.run();
            }
        });
        JMenuItem miRemove = new JMenuItem("Eliminar");
        miRemove.addActionListener(e -> {
            if (onRemove != null) {
                onRemove.run();
            }
        });
        JMenuItem miUp = new JMenuItem("Subir");
        miUp.addActionListener(e -> {
            if (onUp != null) {
                onUp.run();
            }
        });
        JMenuItem miDown = new JMenuItem("Bajar");
        miDown.addActionListener(e -> {
            if (onDown != null) {
                onDown.run();
            }
        });
        popup.add(miAdd);
        popup.add(miEdit);
        popup.add(miRemove);
        popup.addSeparator();
        popup.add(miUp);
        popup.add(miDown);
        list.setComponentPopupMenu(popup);
    }

    private void runSafely(Runnable r) {
        if (r != null) {
            r.run();
        }
    }

    /* ============================== API para el Controller ============================== */
    // ---- Inyección de handlers ----
    public void setOnAddReservada(Runnable r) {
        onAddReservada = r;
        btnAddReservada.addActionListener(e -> runSafely(r));
    }

    public void setOnEditReservada(Runnable r) {
        onEditReservada = r;
        btnEditReservada.addActionListener(e -> runSafely(r));
    }

    public void setOnRemoveReservada(Runnable r) {
        onRemoveReservada = r;
        btnRemoveReservada.addActionListener(e -> runSafely(r));
    }

    public void setOnUpReservada(Runnable r) {
        onUpReservada = r;
    }

    public void setOnDownReservada(Runnable r) {
        onDownReservada = r;
    }

    public void setOnAddOperador(Runnable r) {
        onAddOperador = r;
        btnAddOperador.addActionListener(e -> runSafely(r));
    }

    public void setOnEditOperador(Runnable r) {
        onEditOperador = r;
        btnEditOperador.addActionListener(e -> runSafely(r));
    }

    public void setOnRemoveOperador(Runnable r) {
        onRemoveOperador = r;
        btnRemoveOperador.addActionListener(e -> runSafely(r));
    }

    public void setOnUpOperador(Runnable r) {
        onUpOperador = r;
    }

    public void setOnDownOperador(Runnable r) {
        onDownOperador = r;
    }

    public void setOnAddPuntuacion(Runnable r) {
        onAddPuntuacion = r;
        btnAddPuntuacion.addActionListener(e -> runSafely(r));
    }

    public void setOnEditPuntuacion(Runnable r) {
        onEditPuntuacion = r;
        btnEditPuntuacion.addActionListener(e -> runSafely(r));
    }

    public void setOnRemovePuntuacion(Runnable r) {
        onRemovePuntuacion = r;
        btnRemovePuntuacion.addActionListener(e -> runSafely(r));
    }

    public void setOnUpPuntuacion(Runnable r) {
        onUpPuntuacion = r;
    }

    public void setOnDownPuntuacion(Runnable r) {
        onDownPuntuacion = r;
    }

    public void setOnAddAgrupacion(Runnable r) {
        onAddAgrupacion = r;
        btnAddAgrupacion.addActionListener(e -> runSafely(r));
    }

    public void setOnEditAgrupacion(Runnable r) {
        onEditAgrupacion = r;
        btnEditAgrupacion.addActionListener(e -> runSafely(r));
    }

    public void setOnRemoveAgrupacion(Runnable r) {
        onRemoveAgrupacion = r;
        btnRemoveAgrupacion.addActionListener(e -> runSafely(r));
    }

    public void setOnUpAgrupacion(Runnable r) {
        onUpAgrupacion = r;
    }

    public void setOnDownAgrupacion(Runnable r) {
        onDownAgrupacion = r;
    }

    public void setOnAplicar(Runnable r) {
        onAplicar = r;
        jButton1.addActionListener(e -> runSafely(r));
    }

    public void setOnAceptar(Runnable r) {
        onAceptar = r;
        jButton2.addActionListener(e -> runSafely(r));
    }

    public void setOnCancelar(Runnable r) {
        onCancelar = r;
        jButton3.addActionListener(e -> runSafely(r));
    }

    public void setOnRestaurar(Runnable r) {
        onRestaurar = r;
        jButton4.addActionListener(e -> runSafely(r));
    }

    // ---- Carga/lectura de datos ----
    public void setReservadas(List<String> items) {
        setListModelItems(lstReservadas, items);
    }

    public void setOperadores(List<String> items) {
        setListModelItems(lstOperadores, items);
    }

    public void setPuntuacion(List<String> items) {
        setListModelItems(lstPuntuacion, items);
    }

    public void setAgrupacion(List<String> items) {
        setListModelItems(lstAgrupacion, items);
    }

    public List<String> getReservadas() {
        return getListModelItems(lstReservadas);
    }

    public List<String> getOperadores() {
        return getListModelItems(lstOperadores);
    }

    public List<String> getPuntuacion() {
        return getListModelItems(lstPuntuacion);
    }

    public List<String> getAgrupacion() {
        return getListModelItems(lstAgrupacion);
    }

    public void setComentarioLinea(String s) {
        txtLinea.setText(s == null ? "" : s);
    }

    public void setComentarioBloqueInicio(String s) {
        txtBloqueInicio.setText(s == null ? "" : s);
    }

    public void setComentarioBloqueFin(String s) {
        txtBloqueFin.setText(s == null ? "" : s);
    }

    public String getComentarioLinea() {
        return txtLinea.getText();
    }

    public String getComentarioBloqueInicio() {
        return txtBloqueInicio.getText();
    }

    public String getComentarioBloqueFin() {
        return txtBloqueFin.getText();
    }

    // ---- Utilidades de selección ----
    public void selectFirstReservada() {
        selectFirst(lstReservadas);
    }

    public void selectFirstOperador() {
        selectFirst(lstOperadores);
    }

    public void selectFirstPuntuacion() {
        selectFirst(lstPuntuacion);
    }

    public void selectFirstAgrupacion() {
        selectFirst(lstAgrupacion);
    }

    public void setEnabledWhileSaving(boolean enabled) {
        tabsConfig.setEnabled(enabled);
        jButton1.setEnabled(enabled);
        jButton2.setEnabled(enabled);
        jButton3.setEnabled(enabled);
        jButton4.setEnabled(enabled);
    }

    /* ============================== Helpers internos ============================== */
    private void setListModelItems(JList<String> list, List<String> items) {
        DefaultListModel<String> model = castToDefault(list.getModel());
        model.clear();
        if (items != null) {
            for (String s : items) {
                model.addElement(s);
            }
        }
        if (model.size() > 0) {
            list.setSelectedIndex(0);
        }
    }

    private List<String> getListModelItems(JList<String> list) {
        DefaultListModel<String> model = castToDefault(list.getModel());
        List<String> out = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) {
            out.add(model.get(i));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private DefaultListModel<String> castToDefault(ListModel<String> m) {
        if (m instanceof DefaultListModel) {
            return (DefaultListModel<String>) m;
        }
        DefaultListModel<String> dm = new DefaultListModel<>();
        // Cargar lo que haya en el modelo actual, si no es DefaultListModel
        for (int i = 0; i < m.getSize(); i++) {
            dm.addElement(m.getElementAt(i));
        }
        return dm;
    }

    private void selectFirst(JList<String> list) {
        if (list.getModel().getSize() > 0) {
            list.setSelectedIndex(0);
        }
    }

    /**
     * Exposición directa de listas y botones, por si el Controller las requiere
     */
    public JList<String> getLstReservadas() {
        return lstReservadas;
    }

    public JList<String> getLstOperadores() {
        return lstOperadores;
    }

    public JList<String> getLstPuntuacion() {
        return lstPuntuacion;
    }

    public JList<String> getLstAgrupacion() {
        return lstAgrupacion;
    }

    public JButton getBtnAplicar() {
        return jButton1;
    }

    public JButton getBtnAceptar() {
        return jButton2;
    }

    public JButton getBtnCancelar() {
        return jButton3;
    }

    public JButton getBtnRestaurar() {
        return jButton4;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabsConfig = new javax.swing.JTabbedPane();
        panelReservadas = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstReservadas = new javax.swing.JList<>();
        panelBotones = new javax.swing.JPanel();
        btnAddReservada = new javax.swing.JButton();
        btnEditReservada = new javax.swing.JButton();
        btnRemoveReservada = new javax.swing.JButton();
        panelOperadores = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstOperadores = new javax.swing.JList<>();
        panelBotones1 = new javax.swing.JPanel();
        btnAddOperador = new javax.swing.JButton();
        btnEditOperador = new javax.swing.JButton();
        btnRemoveOperador = new javax.swing.JButton();
        panelPuntuacion = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstPuntuacion = new javax.swing.JList<>();
        panelBotones2 = new javax.swing.JPanel();
        btnAddPuntuacion = new javax.swing.JButton();
        btnEditPuntuacion = new javax.swing.JButton();
        btnRemovePuntuacion = new javax.swing.JButton();
        panelAgrupacion = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        lstAgrupacion = new javax.swing.JList<>();
        panelBotones3 = new javax.swing.JPanel();
        btnAddAgrupacion = new javax.swing.JButton();
        btnEditAgrupacion = new javax.swing.JButton();
        btnRemoveAgrupacion = new javax.swing.JButton();
        panelComentarios = new javax.swing.JPanel();
        lblLinea = new javax.swing.JLabel();
        txtLinea = new javax.swing.JTextField();
        lblBloqueInicio = new javax.swing.JLabel();
        txtBloqueInicio = new javax.swing.JTextField();
        lblBloqueFin = new javax.swing.JLabel();
        txtBloqueFin = new javax.swing.JTextField();
        panelInformativo = new javax.swing.JPanel();
        lblInformativo = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Configurar lenguaje (config.json)");
        setModal(true);
        setPreferredSize(new java.awt.Dimension(760, 560));

        tabsConfig.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        panelReservadas.setLayout(new java.awt.BorderLayout());

        lstReservadas.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        lstReservadas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstReservadas.setToolTipText("Doble clic para editar; usa Agregar/Eliminar/Subir/Bajar");
        lstReservadas.setVisibleRowCount(-1);
        jScrollPane1.setViewportView(lstReservadas);

        panelReservadas.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        panelBotones.setLayout(new java.awt.GridBagLayout());

        btnAddReservada.setText("Agregar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones.add(btnAddReservada, gridBagConstraints);

        btnEditReservada.setText("Editar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones.add(btnEditReservada, gridBagConstraints);

        btnRemoveReservada.setText("Eliminar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones.add(btnRemoveReservada, gridBagConstraints);

        panelReservadas.add(panelBotones, java.awt.BorderLayout.LINE_START);

        tabsConfig.addTab("Reservadas", panelReservadas);

        panelOperadores.setLayout(new java.awt.BorderLayout());

        lstOperadores.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        lstOperadores.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstOperadores.setToolTipText("Doble clic para editar; usa Agregar/Eliminar/Subir/Bajar");
        lstOperadores.setVisibleRowCount(-1);
        jScrollPane2.setViewportView(lstOperadores);

        panelOperadores.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        panelBotones1.setLayout(new java.awt.GridBagLayout());

        btnAddOperador.setText("Agregar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones1.add(btnAddOperador, gridBagConstraints);

        btnEditOperador.setText("Editar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones1.add(btnEditOperador, gridBagConstraints);

        btnRemoveOperador.setText("Eliminar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones1.add(btnRemoveOperador, gridBagConstraints);

        panelOperadores.add(panelBotones1, java.awt.BorderLayout.LINE_START);

        tabsConfig.addTab("Operadores", panelOperadores);

        panelPuntuacion.setLayout(new java.awt.BorderLayout());

        lstPuntuacion.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        lstPuntuacion.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstPuntuacion.setToolTipText("Doble clic para editar; usa Agregar/Eliminar/Subir/Bajar");
        lstPuntuacion.setVisibleRowCount(-1);
        jScrollPane3.setViewportView(lstPuntuacion);

        panelPuntuacion.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        panelBotones2.setLayout(new java.awt.GridBagLayout());

        btnAddPuntuacion.setText("Agregar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones2.add(btnAddPuntuacion, gridBagConstraints);

        btnEditPuntuacion.setText("Editar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones2.add(btnEditPuntuacion, gridBagConstraints);

        btnRemovePuntuacion.setText("Eliminar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones2.add(btnRemovePuntuacion, gridBagConstraints);

        panelPuntuacion.add(panelBotones2, java.awt.BorderLayout.LINE_START);

        tabsConfig.addTab("Puntuación", panelPuntuacion);

        panelAgrupacion.setLayout(new java.awt.BorderLayout());

        lstAgrupacion.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        lstAgrupacion.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstAgrupacion.setToolTipText("Doble clic para editar; usa Agregar/Eliminar/Subir/Bajar");
        lstAgrupacion.setVisibleRowCount(-1);
        jScrollPane4.setViewportView(lstAgrupacion);

        panelAgrupacion.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        panelBotones3.setLayout(new java.awt.GridBagLayout());

        btnAddAgrupacion.setText("Agregar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones3.add(btnAddAgrupacion, gridBagConstraints);

        btnEditAgrupacion.setText("Editar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones3.add(btnEditAgrupacion, gridBagConstraints);

        btnRemoveAgrupacion.setText("Eliminar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panelBotones3.add(btnRemoveAgrupacion, gridBagConstraints);

        panelAgrupacion.add(panelBotones3, java.awt.BorderLayout.LINE_START);

        tabsConfig.addTab("Agrupación", panelAgrupacion);

        java.awt.GridBagLayout panelComentariosLayout = new java.awt.GridBagLayout();
        panelComentariosLayout.columnWidths = new int[] {0, 10, 0, 10, 0};
        panelComentariosLayout.rowHeights = new int[] {0, 20, 0, 20, 0, 20, 0};
        panelComentarios.setLayout(panelComentariosLayout);

        lblLinea.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblLinea.setText("Comentario de línea:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panelComentarios.add(lblLinea, gridBagConstraints);

        txtLinea.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtLinea.setText("ej.: //");
        txtLinea.setToolTipText("Prefijo para comentario de una línea (puede tener varios caracteres)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        panelComentarios.add(txtLinea, gridBagConstraints);

        lblBloqueInicio.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblBloqueInicio.setText("Bloque: inicio");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        panelComentarios.add(lblBloqueInicio, gridBagConstraints);

        txtBloqueInicio.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtBloqueInicio.setText("ej.: /*");
        txtBloqueInicio.setToolTipText("Delimitadores del comentario de bloque; deben ser distintos.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        panelComentarios.add(txtBloqueInicio, gridBagConstraints);

        lblBloqueFin.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblBloqueFin.setText("Bloque: fin");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        panelComentarios.add(lblBloqueFin, gridBagConstraints);

        txtBloqueFin.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtBloqueFin.setText("ej.: */");
        txtBloqueFin.setToolTipText("Delimitadores del comentario de bloque; deben ser distintos.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        panelComentarios.add(txtBloqueFin, gridBagConstraints);

        panelInformativo.setLayout(new java.awt.BorderLayout());

        lblInformativo.setText("Los comentarios se ignoran durante el análisis. Si el comentario de bloque no se cierra, se considera error.");
        panelInformativo.add(lblInformativo, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        panelComentarios.add(panelInformativo, gridBagConstraints);

        tabsConfig.addTab("Comentarios", panelComentarios);

        getContentPane().add(tabsConfig, java.awt.BorderLayout.CENTER);

        jPanel1.setToolTipText("Valida y guarda en el archivo config.json actual");
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton1.setText("Aplicar");
        jPanel1.add(jButton1);

        jButton2.setText("Aceptar");
        jPanel1.add(jButton2);

        jButton3.setText("Cancelar");
        jPanel1.add(jButton3);

        jButton4.setText("Restaurar predeterminados");
        jPanel1.add(jButton4);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

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
            java.util.logging.Logger.getLogger(ConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ConfigDialog dialog = new ConfigDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddAgrupacion;
    private javax.swing.JButton btnAddOperador;
    private javax.swing.JButton btnAddPuntuacion;
    private javax.swing.JButton btnAddReservada;
    private javax.swing.JButton btnEditAgrupacion;
    private javax.swing.JButton btnEditOperador;
    private javax.swing.JButton btnEditPuntuacion;
    private javax.swing.JButton btnEditReservada;
    private javax.swing.JButton btnRemoveAgrupacion;
    private javax.swing.JButton btnRemoveOperador;
    private javax.swing.JButton btnRemovePuntuacion;
    private javax.swing.JButton btnRemoveReservada;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblBloqueFin;
    private javax.swing.JLabel lblBloqueInicio;
    private javax.swing.JLabel lblInformativo;
    private javax.swing.JLabel lblLinea;
    private javax.swing.JList<String> lstAgrupacion;
    private javax.swing.JList<String> lstOperadores;
    private javax.swing.JList<String> lstPuntuacion;
    private javax.swing.JList<String> lstReservadas;
    private javax.swing.JPanel panelAgrupacion;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelBotones1;
    private javax.swing.JPanel panelBotones2;
    private javax.swing.JPanel panelBotones3;
    private javax.swing.JPanel panelComentarios;
    private javax.swing.JPanel panelInformativo;
    private javax.swing.JPanel panelOperadores;
    private javax.swing.JPanel panelPuntuacion;
    private javax.swing.JPanel panelReservadas;
    private javax.swing.JTabbedPane tabsConfig;
    private javax.swing.JTextField txtBloqueFin;
    private javax.swing.JTextField txtBloqueInicio;
    private javax.swing.JTextField txtLinea;
    // End of variables declaration//GEN-END:variables
}
