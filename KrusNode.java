public class KrusNode{

    private KrusNode parentNode;
    private int size;
    
	public KrusNode(){
        parentNode = this;
        size = 1;
    }

	public int getSize(){
		return size;
	}
	public void setSize(int s){
		size = s;
	}
	public void plusOneSize(){
		size += 1;
	}
    public KrusNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(KrusNode pNode) {
        this.parentNode = pNode;
    }
}