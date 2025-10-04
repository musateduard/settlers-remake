/*******************************************************************************
 * Copyright (c) 2018 - 2019
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

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import go.graphics.DrawModeListener;
import go.graphics.FramerateComputer;
import go.graphics.swing.ContextContainer;
import go.graphics.swing.event.swingInterpreter.GOSwingEventConverter;


public abstract class AsyncContextCreator extends ContextCreator<JPanel> implements Runnable, DrawModeListener {

	protected boolean offscreen = true;
	protected boolean async_resized = false;
	protected boolean clear_offscreen = true;
	private boolean continue_run = true;
	private BufferedImage image = null;
	private IntBuffer pixels;
	private final Thread render_thread = new Thread(this, "AsyncRenderer");

    public abstract void async_init();
    public abstract void async_set_size(int width, int height);
    public abstract void async_refresh();
    public abstract void async_swap_buffers();
    public abstract void async_stop();


	public AsyncContextCreator(ContextContainer container, boolean debug)  {
		super(container, debug);
        return;
	}


	@Override
	public void stop() {
		this.continue_run = false;
        return;
	}


	@Override
	public void initSpecific() {

		this.canvas = new JPanel() {

            @Override
			public void paintComponent(Graphics graphics) {

				super.paintComponent(graphics);

				if (first_draw) {
					SwingUtilities.windowForComponent(this).addKeyListener(new GOSwingEventConverter(parent, parent));
					first_draw = false;
				}

				if (offscreen) {
					synchronized (wnd_lock) {
						graphics.drawImage(image, 0, 0, null);
						graphics.dispose();
					}
				}

                else {
					graphics.drawString("Press m to enable offscreen transfer", width / 3, height / 2);
				}

				if (fpsLimit == 0) {
                    this.repaint();
                }

                return;
			}
		};

		this.render_thread.start();
        return;
	}


	@Override
	public void run() {

		synchronized (this.wnd_lock) {
			this.width = this.new_width;
			this.height = this.new_height;
		}

        this.async_init();

		FramerateComputer fpsComputer = new FramerateComputer();

		while (this.continue_run) {

			try {

				if (this.change_res) {

					synchronized (this.wnd_lock) {

						this.width = this.new_width;
						this.height = this.new_height;

						if (this.async_resized) {
                            this.async_resized = false;
						}

                        else {
                            this.async_set_size(this.width, this.height);
						}

						Thread.sleep(20); // we must wait a bit because X is async and our window must not be resized in time otherwise
                        this.parent.resizeContext(this.width, this.height);

                        this.image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_3BYTE_BGR);
                        this.pixels = BufferUtils.createIntBuffer(this.width * this.height);
					}

                    this.change_res = false;
				}

                this.async_refresh();

				this.parent.draw();
				this.parent.finishFrame();

				if (this.offscreen) {

					synchronized (this.wnd_lock) {

                        this.parent.readFramebuffer(this.pixels, this.width, this.height);

						for (int offsetX = 0; offsetX != this.width; offsetX++) {
							for (int offsetY = 0; offsetY != this.height; offsetY++) {
                                this.image.setRGB(offsetX, this.height - offsetY - 1, this.pixels.get(offsetY * this.width + offsetX));
							}
						}
					}
				}

				if (!this.offscreen || this.clear_offscreen) {
					if (this.clear_offscreen && !this.offscreen) {
                        this.parent.clearFramebuffer();
                        this.clear_offscreen = false;
					}

                    this.async_swap_buffers();

                    if (this.fpsLimit != 0) {
                        fpsComputer.nextFrame(this.fpsLimit);
                    }
				}

			}

            catch (ContextException ignored) {
                // do nothing
			}

            catch (Throwable thrown) {
				thrown.printStackTrace();
			}
		}

        this.async_stop();
        return;
	}


	@Override
	public void changeDrawMode() {

		this.offscreen = !this.offscreen;
        this.clear_offscreen = true;

        return;
	}
}