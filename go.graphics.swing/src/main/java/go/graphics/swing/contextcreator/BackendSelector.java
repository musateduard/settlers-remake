/*******************************************************************************
 * Copyright (c) 2018
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
package go.graphics.swing.contextcreator;

import java.util.Arrays;
import java.util.stream.Stream;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import org.lwjgl.system.Platform;

import go.graphics.swing.ContextContainer;


public class BackendSelector extends JComboBox<EBackendType> {

	private EBackendType current_item = null;
	public static final EBackendType FALLBACK_BACKEND = EBackendType.GLFW;


    public BackendSelector() {

        super(availableBackends().toArray(EBackendType[]::new));

        this.setEditable(false);
        this.addActionListener(this);

        return;
    }


	@Override
	public void actionPerformed(ActionEvent actionEvent) {

		super.actionPerformed(actionEvent);

		if (actionEvent.getActionCommand().equals("comboBoxChanged")) {

			Object item = this.getSelectedItem();

            if (item == null || item instanceof String) {
				this.setSelectedItem(this.current_item);
				return;
			}

			EBackendType backendItem = (EBackendType) item;

			if (backendItem.platform != null && backendItem.platform != Platform.get()) {
                this.setSelectedItem(this.current_item);
				BackendSelector.this.hidePopup();
				JOptionPane.showMessageDialog(BackendSelector.this.getParent(), backendItem.cc_name + " is only available on " + backendItem.platform);
			}

            else {
                this.current_item = backendItem;
			}
		}

        return;
	}


	private static Stream<EBackendType> availableBackends() {
        Stream<EBackendType> backendList = Arrays.stream(EBackendType.values()).filter((item) -> item.available(Platform.get()));
		return backendList;
	}


	public static EBackendType getBackendByName(String name) {
        EBackendType backend = BackendSelector.availableBackends().filter((item) -> item.cc_name.equalsIgnoreCase(name)).findFirst().orElse(EBackendType.DEFAULT);
        return backend;
	}


	public static ContextCreator<?> createBackend(ContextContainer container, EBackendType backend, boolean debug) {

		EBackendType realBackend = backend;

		if (backend == null || backend == EBackendType.DEFAULT) {
			// first of all usable and suitable backends sorted for being default
			realBackend = BackendSelector.availableBackends().filter((item) -> item.default_for == Platform.get()).sorted().findFirst().orElse(FALLBACK_BACKEND);
		}

        ContextCreator<?> context = realBackend.createContext(container, debug);
		return context;
	}
}