package experiment.os.command_parser;

import experiment.os.Session;
import experiment.os.block.base.DiskINode;
import experiment.os.block.base.File;
import experiment.os.exception.NoSuchFileOrDirectory;
import experiment.os.exception.PermisionException;
import experiment.os.myEnum.AuthorityType;
import experiment.os.myEnum.FileType;
import experiment.os.system.BFD;
import experiment.os.user.User;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.internal.runners.model.EachTestNotifier;

import java.text.SimpleDateFormat;

public class AuthorityExecutor implements Executor {

    private StringBuilder sb;
    private BFD bfd = BFD.getInstance();

    @Override
    public String excute(String command, String[] args, String[] currentPath, Session session) {
        switch (command) {
            case "chgrp":
                return chgrp(args, currentPath, session);

            case "chmod":
                return chmod(args, currentPath, session);

            case "stat":
                return stat(args, currentPath, session);
        }
        return "";
    }

    private String stat(String[] args, String[] currentPath, Session session) {
        sb = new StringBuilder();
        User excutor = session.getUser();
        try {
            String[] path = getCombinationPath(args[0], currentPath);
            Integer[] eachIndex = bfd.getEachIndex(path);
            if (!bfd.hasAuthority(ArrayUtils.subarray(eachIndex, 0, eachIndex.length - 1), excutor, AuthorityType.EXCUTE) ||
                    !bfd.hasAuthority(new Integer[]{eachIndex[eachIndex.length - 1]}, excutor, AuthorityType.READ)) {
                throw new PermisionException("stat");
            }

            int inodeIndex = eachIndex[eachIndex.length - 1];
            DiskINode iNode = bfd.get(inodeIndex);
            sb.append("size: " + iNode.getSize() + "\t");
            sb.append("Blocks:" + (((iNode.getSize() - 1) / File.FILE_TEXT_MAX_LENGTH + 1) + "\t"));
            sb.append("FileType" + iNode.getFileType() + "\n");
            sb.append("Inode:" + inodeIndex + "\t");
            sb.append("Link:" + iNode.getQuoteNum() + "\t");

            short mode = iNode.getMode();
            int otherMode = mode % 8;
            mode /= 8;
            int groupMode = mode % 8;
            mode /= 8;
            int createMode = mode % 8;
            sb.append("Access: (" + createMode + groupMode + otherMode);
            sb.append("Uid: (" + excutor.getName() + "/" + iNode.getUserId() + ")\t");
            sb.append("Gid: (" + excutor.getName() + "/" + iNode.getGroupId() + ")\n");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = formatter.format(iNode.getModifyTime());
            String strDate2 = formatter.format(iNode.getCreateTime());
            sb.append("CreateTime: " + strDate2 + "\n");
            sb.append("ModifyTime: " + strDate + "\n");

        } catch (Exception e) {
            sb.append(e + "\n");
        } finally {
            return sb.toString();
        }

    }

    private String chgrp(String[] args, String[] currentPath, Session session) {
        sb = new StringBuilder();
        // one arg
        try {
            session.getUser().setGid(Short.valueOf(args[0]));
            sb.append("modify group id success\n");
        } catch (Exception e) {
            sb.append("modify group id fail\n");
        } finally {
            return sb.toString();
        }
    }

    private String chmod(String[] args, String[] currentPath, Session session) {
        sb = new StringBuilder();
        if (args.length < 2) {
            return "need param\n";
        }

        try {
            String[] path = getCombinationPath(args[1], currentPath);
            Integer[] eachIndex = bfd.getEachIndex(path);

            // get target inode
            DiskINode diskINode = bfd.get(eachIndex[eachIndex.length - 1]);
            if (diskINode.getUserId() == session.getUser().getUid()) {
                // parse and set auth
                Integer mode = Integer.valueOf(args[0]);
                int otherAuth = mode % 10;
                mode /= 10;
                int groupAuth = mode % 10;
                mode /= 10;
                int createAuth = mode % 10;
                int finalMode = (createAuth << 6) + (groupAuth << 3) + otherAuth;
                diskINode.setMode((short) finalMode);

            } else {
                sb.append("你不是创建者, 不能给权限\n");
            }
        } catch (Exception p) {
            sb.append(p);
        } finally {
            return sb.toString();
        }
    }
}
