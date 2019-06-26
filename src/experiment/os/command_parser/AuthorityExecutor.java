package experiment.os.command_parser;

import experiment.os.Session;
import experiment.os.block.base.DiskINode;
import experiment.os.exception.NoSuchFileOrDirectory;
import experiment.os.system.BFD;

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
        }
        return "";
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
