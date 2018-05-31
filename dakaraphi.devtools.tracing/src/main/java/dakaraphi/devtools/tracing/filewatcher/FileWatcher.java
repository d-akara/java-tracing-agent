package dakaraphi.devtools.tracing.filewatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileWatcher {
	private final List<FileTrackingInfo> fileListeners = new ArrayList<>();
	private final FileWatchDaemon fileWatchDaemon;
	
	public static FileWatcher createFileWatcher() throws IOException {
		FileWatchDaemon fileWatchDaemon = new FileWatchDaemon(1000);
		return new FileWatcher(fileWatchDaemon);
	}
	
	public FileWatcher( FileWatchDaemon fileWatchDaemon) {
		this.fileWatchDaemon = fileWatchDaemon;
	}
	
	public FileWatcher start() {
		fileWatchDaemon.start(fileListeners);
		return this;
	}
	
	public void stop() throws IOException {
		fileWatchDaemon.stop();
	}
	
	public FileWatcher addListener(IFileListener listener, File file) throws IOException {
		System.out.println("FileWatcher watching: " + file);
		fileListeners.add(new FileTrackingInfo(listener, file));
		return this;
	}
	
	private Path resolveAsDirectoryPath(File file) {
		if (file.isFile()) {
			Path path = Paths.get(file.toURI());
			return path.getParent();			
		}
		return Paths.get(file.toURI());
	}
	
	
	public static class FileWatchDaemon implements Runnable {
		private Thread fileDaemonThread = null;
		private boolean stop = false;
		private List<FileTrackingInfo> listeners;
		private long interval;
		public FileWatchDaemon(long interval) {
			this.interval = interval;
		}
		
		public void start(List<FileTrackingInfo> listeners) {
			this.listeners = listeners;
			fileDaemonThread = new Thread(this);
			fileDaemonThread.setDaemon(true);
			fileDaemonThread.start();
		}
		
		public void run() {
			System.out.println("FileWatchDaemon started");
            while (!stop) {
                try {
                	Thread.sleep(interval);
                	for (FileTrackingInfo listenerAndPath : listeners) {
                		if (listenerAndPath.isFileModifiedSinceLastCheck()) {
                			listenerAndPath.fileListener.onFileChange();
                		}
                	}
                    
                } catch (InterruptedException ex) {
                    return;
                }
            }
            System.out.println("FileWatchDaemon ended");
		}
		
		public void stop() {
			stop = true;
		}
		
	}
	
	public static class FileTrackingInfo {
		final private IFileListener fileListener;
		final File file;
		public long lastModificationTime;
		public FileTrackingInfo(IFileListener fileListener, File file) {
			this.fileListener = fileListener;
			this.file = file;
			lastModificationTime = file.lastModified();
		}
		
		public boolean isFileModifiedSinceLastCheck() {
			long currentModificationTime = file.lastModified();
			if ( currentModificationTime > lastModificationTime) {
				lastModificationTime = currentModificationTime;
				return true;
			}
			return false;
		}
	}
	
}
