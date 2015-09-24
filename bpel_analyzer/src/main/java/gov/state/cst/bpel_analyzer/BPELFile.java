package gov.state.cst.bpel_analyzer;

import java.nio.file.Path;

public class BPELFile {
	private Path bpelPath;
	
	@SuppressWarnings("unused")
	private BPELFile() {
	}
	
	public BPELFile( Path bpelPath ) {
		this.bpelPath = bpelPath;
	}

	public Path getPath() {
		return this.bpelPath;
	}
}
