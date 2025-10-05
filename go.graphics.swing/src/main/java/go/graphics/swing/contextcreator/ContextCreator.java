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


public abstract class ContextCreator<CanvasComponent extends Component> implements ComponentListener {

	protected int width;
	protected int height;
	protected int newWidth;
	protected int newHeight;
	protected boolean resolutionChanged;
	protected final Object windowLock;
	protected boolean initialDraw;
	protected int fpsLimit;
	protected CanvasComponent canvas;
	protected ContextContainer contextContainer;
	protected boolean debug;

    public abstract void stop();
    public abstract void initSpecific();


	public ContextCreator(ContextContainer areaContainer, boolean debug) {

		this.contextContainer = areaContainer;
		this.debug = debug;

        this.width = 1;
        this.height = 1;
        this.newWidth = 1;
        this.newHeight = 1;
        this.resolutionChanged = true;
        this.windowLock = new Object();
        this.initialDraw = true;
        this.fpsLimit = 0;

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
        this.contextContainer.fatal(message);
		throw new ContextException();
	}


	public void init() {

        this.initSpecific();
        this.contextContainer.addCanvas(this.canvas);
		this.canvas.addComponentListener(this);

        return;
	}


    public void updateFPSLimit(int fpsLimit) {
        this.fpsLimit = fpsLimit;
        return;
    }


	@Override
	public void componentResized(ComponentEvent event) {

		if (!SwingUtilities.windowForComponent(this.canvas).isFocused()) {
            return;
        }

		synchronized (this.windowLock) {

			AffineTransform scaleInfo = this.canvas.getGraphicsConfiguration().getDefaultTransform();

			double scaleX = 1;
			double scaleY = 1;
			if (scaleInfo != null) {
				scaleX = scaleInfo.getScaleX();
				scaleY = scaleInfo.getScaleX();
			}

			this.newWidth = (int) (this.canvas.getWidth() * scaleX);
			this.newHeight = (int) (this.canvas.getHeight() * scaleY);
			this.resolutionChanged = true;

			if (this.newWidth == 0) {
                this.newWidth = 1;
            }

			if (this.newHeight == 0) {
                this.newHeight = 1;
            }
		}

        return;
	}


    @Override
    public void componentShown(ComponentEvent event) {
        return;
    }


	@Override
	public void componentHidden(ComponentEvent event) {
        return;
    }


	@Override
	public void componentMoved(ComponentEvent event) {
        return;
    }
}