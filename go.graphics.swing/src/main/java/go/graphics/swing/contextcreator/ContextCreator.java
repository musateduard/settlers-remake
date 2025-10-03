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

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import javax.swing.SwingUtilities;

import go.graphics.swing.ContextContainer;


public abstract class ContextCreator<Type extends Component> implements ComponentListener {

	protected int width = 1;
	protected int height = 1;
	protected int new_width = 1;
	protected int new_height = 1;
	protected boolean change_res = true;
	protected final Object wnd_lock = new Object();
	protected boolean first_draw = true;
	protected int fpsLimit = 0;
	protected Type canvas;
	protected ContextContainer parent;
	protected boolean debug;

    public abstract void stop();
    public abstract void initSpecific();


	public ContextCreator(ContextContainer ac, boolean debug) {

		this.parent = ac;
		this.debug = debug;

        return;
	}


	public void repaint() {
		this.canvas.repaint();
        return;
	}


	public void requestFocus() {
        this.canvas.requestFocus();
        return;
	}


	protected void error(String message) throws ContextException {
        this.parent.fatal(message);
		throw new ContextException();
	}


	public void init() {

        this.initSpecific();
        this.parent.addCanvas(this.canvas);
		this.canvas.addComponentListener(this);

        return;
	}


    public void updateFPSLimit(int fpsLimit) {
        this.fpsLimit = fpsLimit;
        return;
    }


	@Override
	public void componentResized(ComponentEvent componentEvent) {

        System.out.printf("canvas resized\n");

		if (!SwingUtilities.windowForComponent(this.canvas).isFocused()) {
            return;
        }

		synchronized (this.wnd_lock) {

			AffineTransform scaleInfo = this.canvas.getGraphicsConfiguration().getDefaultTransform();

			double scaleX = 1;
			double scaleY = 1;
			if (scaleInfo != null) {
				scaleX = scaleInfo.getScaleX();
				scaleY = scaleInfo.getScaleX();
			}

			this.new_width = (int) (this.canvas.getWidth() * scaleX);
			this.new_height = (int) (this.canvas.getHeight() * scaleY);
			this.change_res = true;

			if (this.new_width == 0) {
                this.new_width = 1;
            }

			if (this.new_height == 0) {
                this.new_height = 1;
            }
		}

        return;
	}


	@Override
	public void componentHidden(ComponentEvent componentEvent) {
        return;
    }


	@Override
	public void componentMoved(ComponentEvent componentEvent) {
        return;
    }


	@Override
	public void componentShown(ComponentEvent componentEvent) {
        return;
    }
}