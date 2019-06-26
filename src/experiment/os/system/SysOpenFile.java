package experiment.os.system;

import experiment.os.properties.GlobalProperties;

import java.util.HashMap;
import java.util.Map;

public class SysOpenFile {

	private static final int MAX_OPENFILE = GlobalProperties.getInt("systemOpenFile.maxOpenFile");

	// sysIndex, item
	private Map<Integer, SysOpenFileItem> openFileItems = new HashMap<>();

	private static SysOpenFile sysOpenFile;

	private SysOpenFile () {}

	public static SysOpenFile getInstance() {
		if (sysOpenFile == null) {
			sysOpenFile = new SysOpenFile();
		}
		return sysOpenFile;
	}

	/**
	 * 返回系统打开表项的索引
	 * @param diskInodeIndex
	 * @return
	 */
	public int addItem(int diskInodeIndex) {

		for (Map.Entry<Integer, SysOpenFileItem> entry : openFileItems.entrySet()) {
			if (entry.getValue().getDiskInodeIndex() == diskInodeIndex) {
				entry.getValue().incCount();
				return entry.getKey();
			}
		}

		if (openFileItems.size() >= MAX_OPENFILE) {
			return -1;
		}

		int memInodeIndex = MemInodeTable.getInstance().allocINode(diskInodeIndex);
		int unusedIndex = getUnusedIndex();
		openFileItems.put(unusedIndex, new SysOpenFileItem(diskInodeIndex, memInodeIndex));
		return unusedIndex;
	}

	/**
	 *
	 * @param itemIndex
	 * @return
	 */
	public boolean delItem(int itemIndex) {
		SysOpenFileItem sysOpenFileItem = openFileItems.get(itemIndex);
		if (sysOpenFileItem == null) {
			return true;
		}

		boolean delSuccessInMemInode = MemInodeTable.getInstance().deleteINode(sysOpenFileItem.getMemInodeIndex());
		if (delSuccessInMemInode) {
			openFileItems.remove(itemIndex);
			return true;
		} else {
			// 只有在其他用户也打开的时候才删除失败
			return false;
		}

//		if (sysOpenFileItem.decCount()) {
//			MemInodeTable.getInstance().deleteINode(sysOpenFileItem.getMemInodeIndex());
//			openFileItems.remove(sysOpenFileItem);
//			return true;
//		} else {
//			return false;
//		}

	}

	public MemInode getMemInodeByDiskInodeIndex(int index) {
		for (SysOpenFileItem sysOpenFileItem : openFileItems.values()) {
			if (sysOpenFileItem.getDiskInodeIndex() == index) {
				return MemInodeTable.getInstance().get(sysOpenFileItem.getMemInodeIndex());
			}
		}
		return null;
	}

	private int getUnusedIndex() {
		for (int i = 0; i < 20; i++) {
			if (!openFileItems.containsKey(i)) {
				return i;
			}
		}
		return 100;
	}
}