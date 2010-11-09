package net.swingingblue.flickruploader.restful;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

/**
 * @see http://efreedom.com/Question/1-3213899/Grab-Progress-Http-POST-File-Upload-Android
 * @author findup
 *
 */
public class CountingMultipartEntity extends MultipartEntity  {

    private final ProgressListener listener;
    private long contentLength = 0;

    public CountingMultipartEntity(final ProgressListener listener) {
        super();
        this.listener = listener;
    }

    public CountingMultipartEntity(final HttpMultipartMode mode, final ProgressListener listener) {
        super(mode);
        this.listener = listener;
    }

    public CountingMultipartEntity(HttpMultipartMode mode, final String boundary,
            final Charset charset, final ProgressListener listener) {
        super(mode, boundary, charset);
        this.listener = listener;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream, this.listener, this.contentLength));
    }

    @Override
	public long getContentLength() {
		this.contentLength = super.getContentLength();
		return this.contentLength;
	}

    /**
     * POST通信中コールバック interface
     * @author findup
     */
    public static interface ProgressListener {
        void transferred(long num, long totalsize);
    }

	public static class CountingOutputStream extends FilterOutputStream {

        private final ProgressListener listener;
        private long transferred;
        private final long contentLength;

        public CountingOutputStream(final OutputStream out,
                final ProgressListener listener,
                final long contentLength) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
            this.contentLength = contentLength;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred, this.contentLength);
        }

        public void write(int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred, this.contentLength);
        }
    }
}