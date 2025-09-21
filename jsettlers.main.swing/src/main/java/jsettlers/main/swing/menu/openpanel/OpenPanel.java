/*******************************************************************************
 * Copyright (c) 2016 - 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.main.swing.menu.openpanel;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jsettlers.common.utils.collections.ChangingList;
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.lookandfeel.ELFStyle;


/**
 * Panel to open an existing map
 *
 * @author Andreas Butti
 *
 */
public class OpenPanel extends JPanel implements DocumentListener {
	private static final long serialVersionUID = 1L;

	/**
	 * List with the Maps to select
	 */
	private final JList<MapLoader> mapList;

	/**
	 * Filtered list model
	 */
	private final DefaultListModel<MapLoader> listModelFiltered = new DefaultListModel<>();

	/**
	 * Filter buttons
	 */
	private final JPanel filterPanel = new JPanel();

	/**
	 * Search Textfield
	 */
	private final SearchTextField searchTextField;

	/**
	 * Unfiltered map list
	 */
	private List<MapLoader> availableMaps;

	/**
	 * Currently active filter
	 */
	private EMapFilter currentFilter = EMapFilter.ALL;


	/**
	 * Constructor
	 *
	 * @param maps {@link ChangingList<MapLoader>} list of maps to display
     * @param mapSelectedListener {@link IMapSelectedListener} action listener for selected map
	 */
	public OpenPanel(final ChangingList<? extends MapLoader> maps, IMapSelectedListener mapSelectedListener) {

        this(maps.getItems(), mapSelectedListener);
		maps.setListener((changedLister) -> this.setMapLoaders(changedLister.getItems()));

        return;
	}


	/**
	 * Constructor
	 *
     * @param maps {@link ChangingList<MapLoader>} list of maps to display
     * @param mapSelectedListener {@link IMapSelectedListener} action listener for selected map
	 */
	public OpenPanel(final List<? extends MapLoader> maps, IMapSelectedListener mapSelectedListener) {
		this(maps, mapSelectedListener, new MapListCellRenderer());
        return;
	}


	/**
	 * Constructor
	 *
     * @param maps {@link ChangingList<MapLoader>} list of maps to display
     * @param mapSelectedListener {@link IMapSelectedListener} action listener for selected map
	 * @param cellRenderer {@link ListCellRenderer<MapLoader>} Cell renderer to use
	 */
	public OpenPanel(
        final List<? extends MapLoader> maps,
        final IMapSelectedListener mapSelectedListener,
        final ListCellRenderer<MapLoader> cellRenderer) {

		this.setMapLoadersWithoutSearchChanged(maps);
		this.setLayout(new BorderLayout());

		this.initFilter();

		this.searchTextField = new SearchTextField();
		this.searchTextField.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);

		Box box = Box.createVerticalBox();
		box.add(this.filterPanel);
		box.add(this.searchTextField);

		this.add(box, BorderLayout.NORTH);

        this.searchTextField.getDocument().addDocumentListener(this);

        MouseAdapter mapClickListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {

                if (event.getClickCount() == 2) {
                    if (mapSelectedListener != null) {
                        mapSelectedListener.mapSelected(getSelectedMap());
                    }
                }

                return;
            }
        };

		this.mapList = new JList<>(this.listModelFiltered);
		this.mapList.setCellRenderer(cellRenderer);
		this.mapList.addMouseListener(mapClickListener);
		this.mapList.setOpaque(false);

		this.add(new JScrollPane(this.mapList), BorderLayout.CENTER);

		this.searchChanged();

		if (maps.size() > 0) {
			this.mapList.setSelectedIndex(0);
		}

        return;
	}


	public void addMapLoader(MapLoader newMap) {
		availableMaps.add(newMap);
		searchChanged();
	}

	public void setMapLoaders(final List<? extends MapLoader> maps) {
		setMapLoadersWithoutSearchChanged(maps);
		searchChanged();
	}

	private void setMapLoadersWithoutSearchChanged(final List<? extends MapLoader> maps) {
		availableMaps = new ArrayList<>(maps);
		Collections.sort(availableMaps);
	}

	/**
	 * Initialize the filter buttons
	 */
	private void initFilter() {
		JLabel filterLabel = new JLabel(Labels.getString("mapfilter.title"));
		filterLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);

		filterPanel.add(filterLabel);

		boolean first = true;
		ButtonGroup group = new ButtonGroup();
		for (final EMapFilter filter : EMapFilter.values()) {
			JToggleButton bt = new JToggleButton(filter.getName());
			bt.putClientProperty(ELFStyle.KEY, ELFStyle.TOGGLE_BUTTON_STONE);
			bt.addActionListener(e -> {
				currentFilter = filter;
				searchChanged();
			});

			if (first) {
				first = false;
				bt.setSelected(true);
			}

			group.add(bt);
			filterPanel.add(bt);
		}
	}

	/**
	 * Search has changed, update the list
	 */
	protected void searchChanged() {
		String search = searchTextField.getText().toLowerCase(Locale.ENGLISH);

		listModelFiltered.clear();

		availableMaps.stream()
				.filter(currentFilter::filter)
				.filter(mapLoader -> matchesSearch(mapLoader, search))
				.forEach(listModelFiltered::addElement);
	}

	/**
	 * Checks if a map matches the search criteria
	 *
	 * @param m
	 *            Map
	 * @param search
	 *            Criteria
	 * @return true if yes, false if no
	 */
	private boolean matchesSearch(MapLoader m, String search) {
		if (search.isEmpty()) {
			return true;
		}

		if (m.getMapName().toLowerCase(Locale.ENGLISH).contains(search)) {
			return true;
		}
		if (m.getDescription().toLowerCase(Locale.ENGLISH).contains(search)) {
			return true;
		}
		return m.getMapId().toLowerCase(Locale.ENGLISH).contains(search);

	}


	/**
	 * @return The selected map
	 */
	public MapLoader getSelectedMap() {
		MapLoader selectedMap = this.mapList.getSelectedValue();
        return selectedMap;
	}


	/**
	 * @return true if there are no maps in the list
	 */
	public boolean isEmpty() {
		return availableMaps.isEmpty();
	}


    @Override
    public void removeUpdate(DocumentEvent event) {
        this.searchChanged();
        return;
    }


    @Override
    public void insertUpdate(DocumentEvent event) {
        this.searchChanged();
        return;
    }


    @Override
    public void changedUpdate(DocumentEvent event) {
        this.searchChanged();
        return;
    }
}