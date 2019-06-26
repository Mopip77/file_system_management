package experiment.os.user;

/*文件目录项*/
public class UserOpenFileItem {

	private int diskInodeIndex;
	private int sysOpenFileOffset;
	private String[] path;

	public UserOpenFileItem(int diskInodeIndex, int sysOpenFileOffset, String[] path) {
		this.diskInodeIndex = diskInodeIndex;
		this.sysOpenFileOffset = sysOpenFileOffset;
		this.path = path;
	}

	public int getDiskInodeIndex() {
		return diskInodeIndex;
	}

	public void setDiskInodeIndex(int diskInodeIndex) {
		this.diskInodeIndex = diskInodeIndex;
	}

	public int getSysOpenFileOffset() {
		return sysOpenFileOffset;
	}

	public void setSysOpenFileOffset(int sysOpenFileOffset) {
		this.sysOpenFileOffset = sysOpenFileOffset;
	}

	public String[] getPath() {
		return path;
	}

	public void setPath(String[] path) {
		this.path = path;
	}
}