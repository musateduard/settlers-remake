package go.graphics.swing;

import go.graphics.swing.vulkan.AbstractVulkanOutput;
import go.graphics.swing.vulkan.VulkanSurfaceOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.vulkan.VkInstance;

import java.awt.Component;
import java.awt.LayoutManager;
import java.nio.IntBuffer;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import go.graphics.GLDrawContext;
import go.graphics.event.GOEventHandlerProvider;
import go.graphics.swing.contextcreator.BackendSelector;
import go.graphics.swing.contextcreator.ContextCreator;
import go.graphics.swing.contextcreator.EBackendType;
import go.graphics.swing.contextcreator.JAWTContextCreator;
import go.graphics.swing.contextcreator.ContextException;
import go.graphics.swing.opengl.LWJGLDrawContext;
import go.graphics.swing.vulkan.VulkanDrawContext;


public abstract class ContextContainer extends JPanel implements GOEventHandlerProvider {

	protected ContextCreator<?> contextCreator;
	protected GLDrawContext context;
	private AbstractVulkanOutput vulkanOutput;
	private final boolean debug;
	protected float guiScale;


	public ContextContainer(EBackendType backend, LayoutManager layout, boolean debug) {

        this.setLayout(layout);
		this.debug = debug;
        this.guiScale = 0.00f;

		try {
			this.contextCreator = BackendSelector.createBackend(this, backend, debug);
			this.contextCreator.init();
		}

        catch (Exception exception) {
			exception.printStackTrace();
			this.fatal("Could not create opengl context through " + backend.cc_name);
		}

        return;
	}


	public void fatal(String message) {

		SwingUtilities.invokeLater(
            () -> {
                JOptionPane.showMessageDialog(null, message + "\nPress ok to exit", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        );

		System.err.println(message);
        return;
	}


	public void resizeContext(int width, int height) throws ContextException {

		if (this.context == null) {
            throw new ContextException();
        }

		this.context.resize(width, height);
        return;
	}


    public void finishFrame() {
        this.context.finishFrame();
        return;
    }


	public void createVulkanContext(VkInstance instance, AbstractVulkanOutput vkOutput) {

        if (this.context != null) {
            this.context.invalidate();
        }

		this.vulkanOutput = vkOutput;

		try {
			this.context = new VulkanDrawContext(instance, vkOutput, this.guiScale);
		}

        catch (Throwable thrown) {
			thrown.printStackTrace();
			this.fatal(thrown.getLocalizedMessage());
		}

        return;
	}


	public void createGLContext() {

        if (this.contextCreator instanceof JAWTContextCreator) {
            ((JAWTContextCreator) this.contextCreator).makeCurrent(true);
        }

		if (this.context != null) {
            this.context.invalidate();
        }

		this.vulkanOutput = null;

		GLCapabilities caps = GL.createCapabilities();

		try {

			if (caps.OpenGL20) {
				this.context = new LWJGLDrawContext(caps, this.debug, this.guiScale);
			}

            else {
				this.errorGLVersion();
			}
		}

        catch (Throwable thrown) {
			thrown.printStackTrace();
			this.fatal(thrown.getLocalizedMessage());
		}

        return;
	}


	private void errorGLVersion() {
		this.fatal("JSettlers needs at least OpenGL 2.0");
        return;
	}


	/**
	 * Disposes all textures / buffers that were allocated by this context.
	 */
	public void disposeAll() {

		if (this.context != null) {
            this.context.invalidate();
        }

		this.context = null;

		if (this.contextCreator != null) {
            this.contextCreator.stop();
        }

		this.contextCreator = null;
        return;
	}


    public void draw() throws ContextException {

        if (this.context == null) {
            throw new ContextException();
        }

        this.context.startFrame();
        return;
    }


	/**
	 * Forward the focus call to the Input canvas
	 */
	@Override
	public void requestFocus() {
		this.contextCreator.requestFocus();
        return;
	}


	public void addCanvas(Component canvas) {
		this.add(canvas);
        return;
	}


	public void updateFPSLimit(int fpsLimit) {

		if (this.contextCreator != null) {
            this.contextCreator.updateFPSLimit(fpsLimit);
        }

        return;
	}


	public void swapBuffersVk() throws ContextException {

		if (this.context == null) {
            throw new ContextException();
        }

		((VulkanDrawContext) this.context).endFrame();
        return;
	}


	public void readFramebuffer(IntBuffer pixels, int width, int height) {

		if (this.context instanceof VulkanDrawContext) {
			((VulkanDrawContext) this.context).readFramebuffer(pixels, width, height);
		}

        else {
			((LWJGLDrawContext) this.context).readFramebuffer(pixels, width, height);
		}

        return;
	}


	public void clearFramebuffer() {

		if (this.context instanceof VulkanDrawContext) {
			((VulkanDrawContext) this.context).clearFramebuffer();
		}

        else {
			((LWJGLDrawContext) this.context).clearFramebuffer();
		}

        return;
	}


	public void removeSurface() {

        if (this.vulkanOutput instanceof VulkanSurfaceOutput) {
			((VulkanSurfaceOutput) this.vulkanOutput).removeSurface();
		}

        return;
	}
}