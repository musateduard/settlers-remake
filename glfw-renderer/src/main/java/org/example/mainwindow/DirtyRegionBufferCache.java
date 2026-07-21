package org.example.mainwindow;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.function.Supplier;


/**
 * Marks dirty spans in a CPU staging buffer and uploads them to a {@link VertexBuffer}.
 */
public class DirtyRegionBufferCache {

    private final Supplier<ByteBuffer> readBuffer;
    private final int bytesPerField;
    private final Supplier<VertexBuffer> meshSupplier;
    private final BitSet[] updated;
    private final int lineWidth;


    public DirtyRegionBufferCache(
        Supplier<ByteBuffer> buffer,
        int bytesPerField,
        Supplier<VertexBuffer> meshSupplier,
        int lineWidth) {

        this.bytesPerField = bytesPerField;
        this.lineWidth = lineWidth;
        this.meshSupplier = meshSupplier;
        this.readBuffer = buffer;

        int lines = buffer.get().capacity() / bytesPerField / lineWidth;
        this.updated = new BitSet[lines];
        for (int i = 0; i != lines; i++) {
            this.updated[i] = new BitSet(lineWidth);
        }

        return;
    }


    public void markLine(int line, int start, int count) {
        synchronized (this.updated[line]) {
            this.updated[line].set(start, start + count);
        }
        return;
    }


    public void clearCacheRegion(int line, int start, int end) {
        synchronized (this.updated[line]) {
            int urEnd = start;
            while (urEnd < end) {
                int urStart = this.updated[line].nextSetBit(urEnd);
                if (urStart > end || urStart == -1) {
                    return;
                }
                urEnd = this.updated[line].nextClearBit(urStart);
                if (urEnd > end || urEnd == -1) {
                    urEnd = end;
                }
                this.updateRegion(line, urStart, urEnd);
                this.updated[line].clear(urStart, urEnd);
            }
        }
        return;
    }


    private void updateRegion(int line, int start, int end) {
        start += line * this.lineWidth;
        end += line * this.lineWidth;

        ByteBuffer realBuffer = this.readBuffer.get();
        realBuffer.limit(end * this.bytesPerField);
        realBuffer.position(start * this.bytesPerField);
        this.meshSupplier.get().updateVertexBuffer((long) start * this.bytesPerField, realBuffer);
        realBuffer.limit(realBuffer.capacity());
        return;
    }
}