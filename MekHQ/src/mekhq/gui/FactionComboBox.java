/**
 * 
 */
package mekhq.gui;

import java.awt.Component;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import mekhq.campaign.universe.Faction;
import mekhq.gui.model.SortedComboBoxModel;

/**
 * Combo box for choosing a faction by full name that accounts for
 * the fact that full names are not always unique within the faction's
 * era.
 * 
 * @author Neoancient
 *
 */
public class FactionComboBox extends JComboBox<Map.Entry<String, String>> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1352706316707722054L;

	@SuppressWarnings("serial")
	public FactionComboBox() {
		Comparator comp = new Comparator<Map.Entry<String, String>>() {

			@Override
			public int compare(Entry<String, String> arg0,
					Entry<String, String> arg1) {
				return arg0.getValue().compareTo(arg1.getValue());
			}
			
		};
		setModel(new SortedComboBoxModel<Map.Entry<String, String>>(comp));
		setRenderer(new DefaultListCellRenderer() {
			@Override
			public  Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value != null) {
					setText(((Map.Entry<String, String>)value).getValue());
				}
				return this;
			}			
		});
	}
	
	public void addFactionEntries(Collection<String> list, int era) {
		HashMap<String, String> map = new HashMap<String, String>();
		HashSet<String> collisions = new HashSet<String>();
		for (String key : list) {
			String fullName = Faction.getFaction(key).getFullName(era);
			if (map.containsValue(fullName)) {
				collisions.add(fullName);
			}
			map.put(key, fullName);
		}
		for (String key : map.keySet()) {
			if (collisions.contains(map.get(key))) {
				map.put(key, map.get(key) + " (" + key + ")");
			}
		}
		for (Map.Entry<String, String> entry : map.entrySet()) {
			addItem(entry);
		}
	}

	public String getSelectedItemKey() {
		if (getSelectedItem() == null) {
			return null;
		}
		return ((Map.Entry<String, String>)getSelectedItem()).getKey();
	}

	public String getSelectedItemValue() {
		if (getSelectedItem() == null) {
			return null;
		}
		return ((Map.Entry<String, String>)getSelectedItem()).getValue();
	}
	
	public void setSelectedItemByKey(String key) {
		for (int i = 0; i < getModel().getSize(); i++) {
			if (key.equals(((Map.Entry<String, String>)(getModel().getElementAt(i))).getKey())) {
				setSelectedIndex(i);
				return;
			}
		}
	}
}
