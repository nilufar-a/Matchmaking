package TronPackage;

public class UniqueID {
	private static int idCounter = 0;

	public static int createID()
	{
	    return idCounter++;
	}    
}
