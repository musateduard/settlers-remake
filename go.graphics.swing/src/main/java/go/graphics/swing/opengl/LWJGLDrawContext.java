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
    private final Matrix4f global = new Matrix4f();
    private final Matrix4f mat = new Matrix4f();
    private final FloatBuffer materialBuffer = BufferUtils.createFloatBuffer(16);
    private LWJGLDebugOutput debugOutput = null;
    final GLCapabilities glcaps;
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


	public LWJGLDrawContext(GLCapabilities glcaps, boolean debug, float guiScale) {

		this.glcaps = glcaps;
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

		if (glcaps.GL_ARB_instanced_arrays && glcaps.GL_ARB_uniform_buffer_object && false) {
			this.prog_unified_multi = new ShaderProgram("unified-multi");
		}

		if (glcaps.GL_EXT_draw_instanced && false) {
            this.prog_unified_array = new ShaderProgram("unified-array");
        }

		this.prog_background = new ShaderProgram("background");
		this.prog_unified = new ShaderProgram("unified");
		this.textDrawer = new LWJGLTextDrawer(this, guiScale);

        return;
	}


	private void useProgram(ShaderProgram id) {

		if (id != this.lastProgram) {
			glUseProgram(id.program);
            this.lastProgram = id;
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

			int id = 0;
			if (texture != null) {
				id = texture.getTextureId();
			}

			glActiveTexture(GL_TEXTURE0 + index);
			glBindTexture(GL_TEXTURE_2D, id);
		}

		this.lastTextures = textures;
        return;
	}


	private void bindGeometry(BufferHandle geometry) {

		if (this.lastGeometry != geometry) {

			int id = 0;
			if (geometry != null) {
				id = geometry.getBufferId();
			}

			glBindBuffer(GL_ARRAY_BUFFER, id);
            this.lastGeometry = geometry;
		}

        return;
	}


	private void bindFormat(int format) {

		if (format != this.lastFormat) {
			glBindVertexArray(format);
            this.lastFormat = format;
		}

        return;
	}


	public void updateBufferAt(BufferHandle handle, int pos, ByteBuffer data) {

        this.bindGeometry(handle);
		glBufferSubData(GL_ARRAY_BUFFER, pos, data);

        return;
	}


	private void setObjectLabel(int type, int id, String name) {

		if (this.debugOutput != null && this.glcaps.GL_KHR_debug) {
			KHRDebug.glObjectLabel(type, id, name);
		}

        return;
	}


	public void setGlobalAttributes(float x, float y, float z, float sx, float sy, float sz) {

        this.finishFrame();

		this.global.identity();
		this.global.scale(sx, sy, sz);
		this.global.translate(x, y, z);
		this.global.get(this.materialBuffer);

		for (ShaderProgram shader : this.shaders) {
            this.useProgram(shader);
			glUniformMatrix4fv(shader.global, false, this.materialBuffer);
		}

        return;
	}


	public void resize(int width, int height) {

		glViewport(0, 0, width, height);
		this.mat.setOrtho(0, width, 0, height, -1, 1);
		this.mat.get(this.materialBuffer);

		for (ShaderProgram shader : this.shaders) {
            this.useProgram(shader);
			glUniformMatrix4fv(shader.projection, false, this.materialBuffer);
		}

        return;
	}


	public void setShadowDepthOffset(float depth) {

		for (ShaderProgram shader : this.shaders) {

			if (shader.shadow_depth != -1) {
                this.useProgram(shader);
				glUniform1f(shader.shadow_depth, depth);
			}
		}

        return;
	}


	public void setHeightMatrix(float[] matrix) {

        this.useProgram(this.prog_background);
		glUniformMatrix4fv(this.prog_background.height, false, matrix);

        return;
	}


	@Override
	public BackgroundDrawHandle createBackgroundDrawCall(int vertices, TextureHandle texture) {

		int vao = -1;

		if (this.glcaps.GL_ARB_vertex_array_object) {
            vao = glGenVertexArrays();
        }

		BufferHandle vertexBuffer = new BufferHandle(this, glGenBuffers());

        this.bindGeometry(vertexBuffer);
        this.setObjectLabel(KHRDebug.GL_BUFFER, vertexBuffer.getBufferId(), "background");

		glBufferData(GL_ARRAY_BUFFER, (long) vertices * 6 * 4, GL_DYNAMIC_DRAW);

		BackgroundDrawHandle handle = new BackgroundDrawHandle(this, vao, texture, vertexBuffer);

		if (this.glcaps.GL_ARB_vertex_array_object) {
            this.bindFormat(vao);
            this.setObjectLabel(GL_VERTEX_ARRAY, vao, "background-vao");
            this.fillBackgroundFormat(handle);
		}

		return handle;
	}


	@Override
	public UnifiedDrawHandle createUnifiedDrawCall(int vertices, String name, TextureHandle texture, TextureHandle texture2, float[] data) {

		int vao = -1;

		if (this.glcaps.GL_ARB_vertex_array_object) {
            vao = glGenVertexArrays();
        }

		BufferHandle vertexBuffer = new BufferHandle(this, glGenBuffers());

        this.bindGeometry(vertexBuffer);
        this.setObjectLabel(KHRDebug.GL_BUFFER, vertexBuffer.getBufferId(), name + "-vertices");

		if (data != null) {
			glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		}

        else {
			glBufferData(GL_ARRAY_BUFFER, (long) vertices * (texture != null ? 4 : 2) * 4, GL_DYNAMIC_DRAW);
		}

		UnifiedDrawHandle handle = new UnifiedDrawHandle(this, vao, 0, vertices, texture, texture2, vertexBuffer);

		if (this.glcaps.GL_ARB_vertex_array_object) {
			this.bindFormat(vao);
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

		if (this.glcaps.GL_ARB_vertex_array_object) {
            vao = glGenVertexArrays();
        }

		BufferHandle drawCalls = new BufferHandle(this, glGenBuffers());

		this.bindGeometry(drawCalls);
		this.setObjectLabel(KHRDebug.GL_BUFFER, drawCalls.getBufferId(), name + "-drawcalls");
		glBufferData(GL_ARRAY_BUFFER, MultiDrawHandle.MAX_CACHE_ENTRIES * 12 * 4, GL_STREAM_DRAW);

		MultiDrawHandle handle = new MultiDrawHandle(this, vao, MultiDrawHandle.MAX_CACHE_ENTRIES, source, drawCalls);

		if (this.glcaps.GL_ARB_vertex_array_object) {
			this.bindFormat(vao);
			this.setObjectLabel(GL_VERTEX_ARRAY, vao, name + "-vao");
			this.fillMultiFormat(handle);
		}

		return handle;
	}


	private void fillBackgroundFormat(BackgroundDrawHandle handle) {

		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);

        this.bindGeometry(handle.vertices);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * 4, 3 * 4);
		glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * 4, 5 * 4);

        return;
	}


	private void fillUnifiedFormat(UnifiedDrawHandle uh) {

        this.bindGeometry(uh.vertices);
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

        this.bindGeometry(handle.drawCalls);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 12 * 4, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 12 * 4, 3 * 4);
		glVertexAttribPointer(2, 4, GL_FLOAT, false, 12 * 4, 5 * 4);
		glVertexAttribPointer(3, 3, GL_FLOAT, false, 12 * 4, 9 * 4);

        return;
	}


	private void enableVertArrays(boolean... vertArrays) {

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
            this.bindFormat(call.getVertexArrayId());
		}

        else {
            this.enableVertArrays(true, true, true, true);
            this.fillMultiFormat(call);
		}

        this.useProgram(this.prog_unified_multi);

		glBindBufferBase(GL_UNIFORM_BUFFER, 0, call.sourceQuads.vertices.getBufferId());
		glDrawArraysInstancedARB(GL_TRIANGLE_FAN, 0, 4, call.used);

        return;
	}


	public void drawUnifiedArray(UnifiedDrawHandle call, int primitive, int vertexCount, float[] trans, float[] colors, int array_len) {

		if (call.texture != null) {
            this.bindTextures(call.texture, call.texture2);
        }

		if (call.getVertexArrayId() != -1) {
            this.bindFormat(call.getVertexArrayId());
		}

        else {
			this.enableVertArrays(true, call.texture != null, false, false);
			this.fillUnifiedFormat(call);
		}

		if (this.prog_unified_array != null) {
            this.useProgram(this.prog_unified_array);

			glUniform4fv(this.prog_unified_array.color, colors);
			glUniform4fv(this.prog_unified_array.trans, trans);

			glDrawArraysInstancedARB(primitive, call.offset, vertexCount, array_len);
		}

        else {
            this.useProgram(this.prog_unified);

			for (int index = 0; index != array_len; index++) {

				float int_mode = trans[index * 4 + 3] / 10;
				int mode = (int) Math.floor(int_mode);
				float intensity = (int_mode-mode) * 10 - 1;

				glUniform1i(prog_unified.mode, mode);
				glUniform1fv(prog_unified.color, new float[] {colors[index * 4], colors[index * 4 + 1], colors[index * 4 + 2], colors[index * 4 + 3], intensity});
				glUniform3fv(prog_unified.trans, new float[] {trans[index * 4], trans[index * 4 + 1], trans[index * 4 + 2], 1, 1, 0});

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
        int primitive, int count, int mode,
        float x, float y, float z, float sx, float sy,
        AbstractColor color, float intensity) {

		if (call.texture != null) {
            this.bindTextures(call.texture, call.texture2);
        }

        this.useProgram(this.prog_unified);

		if (call.getVertexArrayId() != -1) {
            this.bindFormat(call.getVertexArrayId());
		}

        else {
			this.enableVertArrays(true, call.texture != null, false, false);
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

			glUniform1fv(this.prog_unified.color, new float[] {red, green, blue, alpha, intensity});
		}

		if (this.ulm != mode) {
            this.ulm = mode;
			glUniform1i(this.prog_unified.mode, mode);
		}

		glUniform3fv(this.prog_unified.trans, new float[] {x, y, z, sx, sy, 0});
		glDrawArrays(primitive, call.offset, count);

        return;
	}


	public void drawBackground(BackgroundDrawHandle handle) {

		this.bindTextures(handle.texture, handle.texture);
		this.useProgram(this.prog_background);

		if (handle.getVertexArrayId() != -1) {
            this.bindFormat(handle.getVertexArrayId());
		}

        else {
			this.enableVertArrays(true, true, true, false);
			this.fillBackgroundFormat(handle);
		}

		int draw_lines = handle.regionCount;

		int[] firsts = new int[draw_lines];
		int[] counts = new int[draw_lines];

		for (int index = 0; index != draw_lines; index++) {
			firsts[index] = handle.regions[index * 2];
			counts[index] = handle.regions[index * 2 + 1];
		}

		glMultiDrawArrays(GL_TRIANGLES, firsts, counts);
        return;
	}


	@SuppressWarnings("WeakerAccess")
	protected class ShaderProgram  {

		public final int program;
		public final int projection;
		public final int global;
		public final int trans;
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
			this.trans = glGetUniformLocation(this.program, "transform");
			this.tex = glGetUniformLocation(this.program, "texHandle");
			this.tex2 = glGetUniformLocation(this.program, "tex2Handle");
			this.color = glGetUniformLocation(this.program, "color");
			this.height = glGetUniformLocation(this.program, "height");
			this.mode = glGetUniformLocation(this.program, "mode");
			this.shadow_depth = glGetUniformLocation(this.program, "shadow_depth");

			if (glcaps.GL_ARB_uniform_buffer_object) {

                this.geometry_data = glGetUniformBlockIndex(this.program, "geometryDataBuffer");

                if (this.geometry_data != -1) {
                    glUniformBlockBinding(this.program, this.geometry_data, 0);
                }
			}

            else {
                this.geometry_data = -1;
			}

            useProgram(this);

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

			if (glcaps.OpenGL33) {
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