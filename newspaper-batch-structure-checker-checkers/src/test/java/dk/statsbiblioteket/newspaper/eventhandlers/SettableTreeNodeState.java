package dk.statsbiblioteket.newspaper.eventhandlers;


import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
* Created with IntelliJ IDEA.
* User: csr
* Date: 10/24/13
* Time: 9:35 AM
* To change this template use File | Settings | File Templates.
*/
class SettableTreeNodeState extends TreeNodeState {
    private TreeNode currentNode;

    SettableTreeNodeState(TreeNode currentNode) {
        this.currentNode = currentNode;
    }

    public void setCurrentNode(TreeNode node) {
        currentNode = node;
    }

    @Override
    public TreeNode getCurrentNode() {
        return currentNode;
    }
}
