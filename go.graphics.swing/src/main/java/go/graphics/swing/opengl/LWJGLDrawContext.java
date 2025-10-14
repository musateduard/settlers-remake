package go.graphics.swing.opengl;

import go.graphics.ImageData;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.KHRDebug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import go.graphics.AbstractColor;
import go.graphics.BackgroundDrawHandle;
import go.graphics.GLDrawContext;
import go.graphics.BufferHandle;
import go.graphics.ManagedHandle;
import go.graphics.MultiDrawHandle;
import go.graphics.TextureHandle;
import go.graphics.UnifiedDrawHandle;
import go.graphics.swing.text.LWJGLTextDrawer;

import static org.lwjgl.opengl.ARBDrawInstanced.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.ARBUniformBufferObject.*;
import static org.lwjgl.opengl.GL20C.*;


public class LWJGLDrawContext extends GLDrawContext {

    private final ArrayList<ShaderProgram> shaders;
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private LWJGLDebugOutput debugOutput = null;
    final GLCapabilities glCapabilities;
    private BufferHandle lastGeometry = null;
    private TextureHandle[] lastTextures = new TextureHandle[2];
    private ShaderProgram lastProgram = null;

    private ShaderProgram prog_unified_multi = null;
    private ShaderProgram prog_unified_array = null;
    private final ShaderProgram prog_background;
    private final ShaderProgram prog_unified;
    private float ulr;
    private float ulg;
    private float ulb;
    private float ula;
    private float uli;
    private float ulm;
    private int lastFormat = 0;
    private boolean[] vertArrays = new boolean[4];


	public LWJGLDrawContext(GLCapabilities glCapabilities, boolean debug, float guiScale) {

		this.glCapabilities = glCapabilities;
		this.shaders = new ArrayList<>();
		this.maxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
		this.maxUniformBlockSize = glGetInteger(GL_MAX_UNIFORM_BLOCK_SIZE);

		if (debug) {
            this.debugOutput = new LWJGLDebugOutput(this);
        }

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);

		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		if (glCapabilities.GL_ARB_instanced_arrays && glCapabilities.GL_ARB_uniform_buffer_object && false) {
			this.prog_unified_multi = new ShaderProgram("unified-multi");
		}

		if (glCapabilities.GL_EXT_draw_instanced && false) {
            this.prog_unified_array = new ShaderProgram("unified-array");
        }

		this.prog_background = new ShaderProgram("background");
		this.prog_unified = new ShaderProgram("unified");
		this.textDrawer = new LWJGLTextDrawer(this, guiScale);

        return;
	}


	private void activateShader(ShaderProgram shaderId) {

		if (shaderId != this.lastProgram) {
			glUseProgram(shaderId.program);
            this.lastProgram = shaderId;
		}

        return;
	}


	public TextureHandle generateTexture(ImageData image, String name) {

		int texture = glGenTextures();
		if (texture == 0) {
			return null;
		}

		TextureHandle textureHandle = new LWJGLTextureHandle(this, texture);
		this.resizeTexture(textureHandle, image);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		this.setObjectLabel(GL11.GL_TEXTURE, texture, name + "-tex");

		return textureHandle;
	}


	public TextureHandle resizeTexture(TextureHandle textureIndex, ImageData image) {

		this.bindTextures(textureIndex, textureIndex);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, image.getReadData32());

		return textureIndex;
	}


	public void updateTexture(TextureHandle texture, int left, int bottom, ImageData image) {

        this.bindTextures(texture, texture);
		glTexSubImage2D(GL_TEXTURE_2D, 0, left, bottom, image.getWidth(), image.getHeight(), GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, image.getReadData32());

        return;
	}


	protected void bindTextures(TextureHandle... textures) {

		for (int index = 0; index < textures.length; index++) {

			TextureHandle texture = textures[index];

			if (this.lastTextures[index] == texture) {
                continue;
            }

			int textureId = 0;
			if (texture != null) {
				textureId = texture.getTextureId();
			}

			glActiveTexture(GL_TEXTURE0 + index);
			glBindTexture(GL_TEXTURE_2D, textureId);
		}

		this.lastTextures = textures;
        return;
	}


	private void bindVertexBufferObject(BufferHandle geometry) {

		if (this.lastGeometry != geometry) {

			int vboId = 0;
			if (geometry != null) {
				vboId = geometry.getBufferId();
			}

			glBindBuffer(GL_ARRAY_BUFFER, vboId);
            this.lastGeometry = geometry;
		}

        return;
	}


	private void bindVertexArrayObject(int vaoId) {

		if (vaoId != this.lastFormat) {
			glBindVertexArray(vaoId);
            this.lastFormat = vaoId;
		}

        return;
	}


	public void updateBufferAt(BufferHandle handle, int pos, ByteBuffer data) {

        this.bindVertexBufferObject(handle);
		glBufferSubData(GL_ARRAY_BUFFER, pos, data);

        return;
	}


	private void setObjectLabel(int type, int id, String name) {

		if (this.debugOutput != null && this.glCapabilities.GL_KHR_debug) {
			KHRDebug.glObjectLabel(type, id, name);
		}

        return;
	}


	public void updateViewMatrix(float offsetX, float offsetY, float offsetZ, float scaleX, float scaleY, float scaleZ) {

        this.finishFrame();

		this.viewMatrix.identity();
		this.viewMatrix.scale(scaleX, scaleY, scaleZ);
		this.viewMatrix.translate(offsetX, offsetY, offsetZ);
		this.viewMatrix.get(this.matrixBuffer);

		for (ShaderProgram shader : this.shaders) {
            this.activateShader(shader);
			glUniformMatrix4fv(shader.global, false, this.matrixBuffer);
		}

        return;
	}


	public void updateProjectionMatrix(int screenWidth, int screenHeight) {

		glViewport(0, 0, screenWidth, screenHeight);
		this.projectionMatrix.setOrtho(0, screenWidth, 0, screenHeight, -1, 1);
		this.projectionMatrix.get(this.matrixBuffer);

		for (ShaderProgram shader : this.shaders) {
            this.activateShader(shader);
			glUniformMatrix4fv(shader.projection, false, this.matrixBuffer);
		}

        return;
	}


	public void setShadowDepthOffset(float depth) {

		for (ShaderProgram shader : this.shaders) {

			if (shader.shadow_depth != -1) {
                this.activateShader(shader);
				glUniform1f(shader.shadow_depth, depth);
			}
		}

        return;
	}


	public void setHeightMatrix(float[] matrix) {

        this.activateShader(this.prog_background);
		glUniformMatrix4fv(this.prog_background.height, false, matrix);

        return;
	}


	@Override
	public BackgroundDrawHandle createBackgroundDrawCall(int vertices, TextureHandle texture) {

		int vao = -1;

		if (this.glCapabilities.GL_ARB_vertex_array_object) {
            vao = glGenVertexArrays();
        }

		BufferHandle vertexBuffer = new BufferHandle(this, glGenBuffers());

        this.bindVertexBufferObject(vertexBuffer);
        this.setObjectLabel(KHRDebug.GL_BUFFER, vertexBuffer.getBufferId(), "background");

		glBufferData(GL_ARRAY_BUFFER, (long) vertices * 6 * 4, GL_DYNAMIC_DRAW);

		BackgroundDrawHandle handle = new BackgroundDrawHandle(this, vao, texture, vertexBuffer);

		if (this.glCapabilities.GL_ARB_vertex_array_object) {
            this.bindVertexArrayObject(vao);
            this.setObjectLabel(GL_VERTEX_ARRAY, vao, "background-vao");
            this.fillBackgroundVertexArrayObject(handle);
		}

		return handle;
	}


	@Override
	public UnifiedDrawHandle createUnifiedDrawCall(int vertices, String name, TextureHandle texture, TextureHandle texture2, float[] data) {

		int vao = -1;

		if (this.glCapabilities.GL_ARB_vertex_array_object) {
            vao = glGenVertexArrays();
        }

		BufferHandle vertexBuffer = new BufferHandle(this, glGenBuffers());

        this.bindVertexBufferObject(vertexBuffer);
        this.setObjectLabel(KHRDebug.GL_BUFFER, vertexBuffer.getBufferId(), name + "-vertices");

		if (data != null) {
			glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		}

        else {
			glBufferData(GL_ARRAY_BUFFER, (long) vertices * (texture != null ? 4 : 2) * 4, GL_DYNAMIC_DRAW);
		}

		UnifiedDrawHandle handle = new UnifiedDrawHandle(this, vao, 0, vertices, texture, texture2, vertexBuffer);

		if (this.glCapabilities.GL_ARB_vertex_array_object) {
			this.bindVertexArrayObject(vao);
			this.setObjectLabel(GL_VERTEX_ARRAY, vao, name + "-vao");
			this.fillUnifiedFormat(handle);
		}

		return handle;
	}


	@Override
	protected MultiDrawHandle createMultiDrawCall(String name, ManagedHandle source) {

		if (this.prog_unified_multi == null) {
            return null;
        }

		int vao = -1;

		if (this.glCapabilities.GL_ARB_vertex_array_object) {
            vao = glGenVertexArrays();
        }

		BufferHandle drawCalls = new BufferHandle(this, glGenBuffers());

		this.bindVertexBufferObject(drawCalls);
		this.setObjectLabel(KHRDebug.GL_BUFFER, drawCalls.getBufferId(), name + "-drawcalls");
		glBufferData(GL_ARRAY_BUFFER, MultiDrawHandle.MAX_CACHE_ENTRIES * 12 * 4, GL_STREAM_DRAW);

		MultiDrawHandle handle = new MultiDrawHandle(this, vao, MultiDrawHandle.MAX_CACHE_ENTRIES, source, drawCalls);

		if (this.glCapabilities.GL_ARB_vertex_array_object) {
			this.bindVertexArrayObject(vao);
			this.setObjectLabel(GL_VERTEX_ARRAY, vao, name + "-vao");
			this.fillMultiFormat(handle);
		}

		return handle;
	}


	private void fillBackgroundVertexArrayObject(BackgroundDrawHandle handle) {

		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);

        this.bindVertexBufferObject(handle.vertices);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * 4, 3 * 4);
		glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * 4, 5 * 4);

        return;
	}


	private void fillUnifiedFormat(UnifiedDrawHandle uh) {

        this.bindVertexBufferObject(uh.vertices);
		glEnableVertexAttribArray(0);

		if (uh.texture != null) {
			glEnableVertexAttribArray(1);

			glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 4, 2 * 4);
		}

        else {
			glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		}

        return;
	}


	private void fillMultiFormat(MultiDrawHandle handle) {

		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);

		ARBInstancedArrays.glVertexAttribDivisorARB(0, 1);
		ARBInstancedArrays.glVertexAttribDivisorARB(1, 1);
		ARBInstancedArrays.glVertexAttribDivisorARB(2, 1);
		ARBInstancedArrays.glVertexAttribDivisorARB(3, 1);

        this.bindVertexBufferObject(handle.drawCalls);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 12 * 4, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 12 * 4, 3 * 4);
		glVertexAttribPointer(2, 4, GL_FLOAT, false, 12 * 4, 5 * 4);
		glVertexAttribPointer(3, 3, GL_FLOAT, false, 12 * 4, 9 * 4);

        return;
	}


	private void enableVertexArrays(boolean... vertArrays) {

		for (int index = 0; index != vertArrays.length; index++) {

			if (vertArrays[index] != this.vertArrays[index]) {

				if (vertArrays[index]) {
					glEnableVertexAttribArray(index);
				}

                else {
					glDisableVertexAttribArray(index);
				}
			}
		}

		this.vertArrays = vertArrays;
        return;
	}


	protected void drawMulti(MultiDrawHandle call) {

		this.bindTextures(call.sourceQuads.texture, call.sourceQuads.texture2);

		if (call.getVertexArrayId() != -1) {
            this.bindVertexArrayObject(call.getVertexArrayId());
		}

        else {
            this.enableVertexArrays(true, true, true, true);
            this.fillMultiFormat(call);
		}

        this.activateShader(this.prog_unified_multi);

		glBindBufferBase(GL_UNIFORM_BUFFER, 0, call.sourceQuads.vertices.getBufferId());
		glDrawArraysInstancedARB(GL_TRIANGLE_FAN, 0, 4, call.used);

        return;
	}


	public void drawUnifiedArray(UnifiedDrawHandle call, int primitive, int vertexCount, float[] trans, float[] colors, int array_len) {

		if (call.texture != null) {
            this.bindTextures(call.texture, call.texture2);
        }

		if (call.getVertexArrayId() != -1) {
            this.bindVertexArrayObject(call.getVertexArrayId());
		}

        else {
			this.enableVertexArrays(true, call.texture != null, false, false);
			this.fillUnifiedFormat(call);
		}

		if (this.prog_unified_array != null) {
            this.activateShader(this.prog_unified_array);

			glUniform4fv(this.prog_unified_array.color, colors);
			glUniform4fv(this.prog_unified_array.transform, trans);

			glDrawArraysInstancedARB(primitive, call.offset, vertexCount, array_len);
		}

        else {
            this.activateShader(this.prog_unified);

			for (int index = 0; index != array_len; index++) {

				float int_mode = trans[index * 4 + 3] / 10;
				int mode = (int) Math.floor(int_mode);
				float intensity = (int_mode-mode) * 10 - 1;

				glUniform1i(prog_unified.mode, mode);
				glUniform1fv(prog_unified.color, new float[] {colors[index * 4], colors[index * 4 + 1], colors[index * 4 + 2], colors[index * 4 + 3], intensity});
				glUniform3fv(prog_unified.transform, new float[] {trans[index * 4], trans[index * 4 + 1], trans[index * 4 + 2], 1, 1, 0});

				glDrawArrays(primitive, call.offset, vertexCount);
			}

			this.ulr = -1;
			this.ulm = -1;
		}

        return;
	}


	@Override
    public void drawUnified(
        UnifiedDrawHandle call,
        int primitive, int vertexCount, int mode,
        float modelX, float modelY, float modelZ, float scaleX, float scaleY,
        AbstractColor color, float intensity) {

        // bind texture
        if (call.texture != null) {
            this.bindTextures(call.texture, call.texture2);
        }

        // activate shader
        this.activateShader(this.prog_unified);

        // set vao
        if (call.getVertexArrayId() != -1) {
            this.bindVertexArrayObject(call.getVertexArrayId());
        }

        else {
            this.enableVertexArrays(true, call.texture != null, false, false);
            this.fillUnifiedFormat(call);
        }

        float red;
        float green;
        float blue;
        float alpha;

        if (color != null) {
            red = color.red;
            green = color.green;
            blue = color.blue;
            alpha = color.alpha;
        }

        else {
            red = green = blue = alpha = 1;
        }

        // update color uniform
        if (this.ulr != red ||
            this.ulg != green ||
            this.ulb != blue ||
            this.ula != alpha ||
            this.uli != intensity) {

            this.ulr = red;
            this.ulg = green;
            this.ulb = blue;
            this.ula = alpha;
            this.uli = intensity;

            float[] colorValue = {
                red, green, blue,
                alpha, intensity
            };

            glUniform1fv(this.prog_unified.color, colorValue);
        }

        // apply mode uniform
        if (this.ulm != mode) {
            this.ulm = mode;
            glUniform1i(this.prog_unified.mode, mode);
        }

        float[] spriteModelMatrix = {
            modelX, modelY, modelZ,
            scaleX, scaleY, 0
        };

        // send sprite position uniform
        glUniform3fv(this.prog_unified.transform, spriteModelMatrix);

        // draw sprite
        glDrawArrays(primitive, call.offset, vertexCount);

        return;
    }


    public void drawBackground(BackgroundDrawHandle drawHandle) {

        this.bindTextures(drawHandle.texture, drawHandle.texture);
        this.activateShader(this.prog_background);

        if (drawHandle.getVertexArrayId() != -1) {
            this.bindVertexArrayObject(drawHandle.getVertexArrayId());
        }

        else {
            this.enableVertexArrays(true, true, true, false);
            this.fillBackgroundVertexArrayObject(drawHandle);
        }

        int lineCount = drawHandle.visibleLineCount;

        int[] lineVertexOffsetList = new int[lineCount];
        int[] lineVertexCount = new int[lineCount];

        for (int index = 0; index < lineCount; index++) {
            lineVertexOffsetList[index] = drawHandle.visibleLineObjectList[index * 2];
            lineVertexCount[index] = drawHandle.visibleLineObjectList[index * 2 + 1];
        }

        glMultiDrawArrays(GL_TRIANGLES, lineVertexOffsetList, lineVertexCount);
        return;
    }


	@SuppressWarnings("WeakerAccess")
	protected class ShaderProgram  {

		public final int program;
		public final int projection;
		public final int global;
		public final int transform;
		public final int tex;
		public final int tex2;
		public final int color;
		public final int height;
		public final int mode;
		public final int shadow_depth;
		public final int geometry_data;
        private final ArrayList<String> attributes = new ArrayList<>();


		protected ShaderProgram(String name) {

			int vertexShader = -1;
			int fragmentShader;

			try {
				vertexShader = createShader(name+".vert", GL_VERTEX_SHADER);
				fragmentShader = createShader(name+".frag", GL_FRAGMENT_SHADER);
			}

            catch (IOException exception) {
				exception.printStackTrace();

				if (vertexShader != -1) {
                    glDeleteShader(vertexShader);
                }

				throw new Error("could not read shader files", exception);
			}

			this.program = glCreateProgram();
			setObjectLabel(KHRDebug.GL_PROGRAM, this.program, name);

			glAttachShader(this.program, vertexShader);
			glAttachShader(this.program, fragmentShader);

			for (int index = 0; index != attributes.size(); index++) {
				glBindAttribLocation(this.program, index, this.attributes.get(index));
			}

            this.link(name);
            this.validate(name);

			glDetachShader(this.program, vertexShader);
			glDetachShader(this.program, fragmentShader);
			glDeleteShader(vertexShader);
			glDeleteShader(fragmentShader);

			String log = glGetProgramInfoLog(this.program);

			if (debugOutput != null && !log.isEmpty()) {
                System.out.print("info log of " + name + "=====\n" + log + "==== end\n");
            }

			if (glGetProgrami(this.program, GL_LINK_STATUS) == 0) {

				glDeleteProgram(this.program);
				throw new Error("Could not link " + name);
			}

			this.projection = glGetUniformLocation(this.program, "projection");
			this.global = glGetUniformLocation(this.program, "globalTransform");
			this.transform = glGetUniformLocation(this.program, "transform");
			this.tex = glGetUniformLocation(this.program, "texHandle");
			this.tex2 = glGetUniformLocation(this.program, "tex2Handle");
			this.color = glGetUniformLocation(this.program, "color");
			this.height = glGetUniformLocation(this.program, "height");
			this.mode = glGetUniformLocation(this.program, "mode");
			this.shadow_depth = glGetUniformLocation(this.program, "shadow_depth");

			if (glCapabilities.GL_ARB_uniform_buffer_object) {

                this.geometry_data = glGetUniformBlockIndex(this.program, "geometryDataBuffer");

                if (this.geometry_data != -1) {
                    glUniformBlockBinding(this.program, this.geometry_data, 0);
                }
			}

            else {
                this.geometry_data = -1;
			}

            activateShader(this);

			if (this.tex != -1) {
                glUniform1i(this.tex, 0);
            }

			if (this.tex2 != -1) {
                glUniform1f(this.tex2, 1);
            }

            shaders.add(this);
            return;
		}


		private void link(String name) {

			glLinkProgram(this.program);

			String log = glGetProgramInfoLog(this.program);

			if (!log.isEmpty()) {
                System.out.print("linker info log of " + name + "=====\n" + log + "==== end\n");
            }

			int[] link_status = new int[1];

			glGetProgramiv(this.program, GL_LINK_STATUS, link_status);

            if (link_status[0] == 0) {
				glDeleteProgram(this.program);
				throw new Error("Could not link " + name);
			}
		}


		private void validate(String name) {

			glValidateProgram(this.program);

			String log = glGetProgramInfoLog(this.program);

			if (!log.isEmpty()) {
                System.out.print("validation info log of " + name + "=====\n" + log + "==== end\n");
            }

			int[] validate_status = new int[1];

			glGetProgramiv(this.program, GL_VALIDATE_STATUS, validate_status);

            if (validate_status[0] == 0) {
				glDeleteProgram(this.program);
				throw new Error("Could not validate " + name);
			}

            return;
		}


		private int createShader(String name, int type) throws IOException {

			if (glCapabilities.OpenGL33) {
				name = "gl33/" + name;
			}

            else {
				name = "gl/" + name;
			}

			StringBuilder source = new StringBuilder();

            try (InputStream shaderFile = getClass().getResourceAsStream("/go/graphics/swing/" + name)) {

                if (shaderFile == null) {
                    return -1;
                }

                BufferedReader inputStream = new BufferedReader(new InputStreamReader(shaderFile));
                String line;

                while ((line = inputStream.readLine()) != null) {

                    if (line.startsWith("attribute") || line.endsWith("//attribute")) {
                        this.attributes.add(line.split(" ")[2].replaceAll(";", ""));
                    }

                    else if (line.equals("//define MAX_GEOMETRY_DATA_QUAD_COUNT")) {
                        line = getManagedHandleDefine();
                    }

                    source.append(line).append("\n");
                }
            }

			int shader = glCreateShader(type);

			if (shader == 0) {
                return -1;
            }

			setObjectLabel(KHRDebug.GL_SHADER, shader, name);
			glShaderSource(shader, source);
			glCompileShader(shader);

			String log = glGetShaderInfoLog(shader);

			if (debugOutput != null && !log.isEmpty()) {
                System.out.print("info log of " + name + "=====\n" + log + "==== end\n");
            }

			if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
				glDeleteShader(shader);
				throw new Error("Could not compile " + name);
			}

			return shader;
		}
	}


	public void clearDepthBuffer() {

        this.finishFrame();
		glClear(GL_DEPTH_BUFFER_BIT);

        return;
	}

	public void clearFramebuffer() {

        this.finishFrame();
		glClear(GL_COLOR_BUFFER_BIT);

        return;
	}


	@Override
	public void startFrame() {

		super.startFrame();
		glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        return;
	}


	public void readFramebuffer(IntBuffer pixels, int width, int height) {

        this.finishFrame();
		glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixels);
		glClear(GL_COLOR_BUFFER_BIT);

        return;
	}
}