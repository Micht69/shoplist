package fr.logica.business;

import java.util.HashMap;

/** One element in a result list, on any list page. */
public class Result extends HashMap<String, Object> {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1L;
	
	public Result() {
	}

	/** rownum starting from 0 */
	public Result(Integer rownum) {
		this.put(Constants.RESULT_ROWNUM, rownum);
	}

    public Key getPk() {
        return (Key) this.get(Constants.RESULT_PK);
    }
    
    public void setPk(Key value) {
    	this.put(Constants.RESULT_PK, value);
    }

    public Integer getRownum() {
        return (Integer) this.get(Constants.RESULT_ROWNUM);
    }
	
}
