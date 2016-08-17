package com.natech.roja.Database;

/**
 * Created by Tshepo on 2016/02/05.
 */
public class DatabaseUtilities {

    //tables
    private static final String LOCALITIES = "localities";
    //columns
    private static final String LOCALITY = "locality";
    private static final String LOCALITY_ID = "locID";
    private static final String [] LOCALITY_ARRAY = {"2_Mmabatho","2_Mafikeng","3_Itsoseng"};

    public String createLocalitiesTable(){
        String sql = "CREATE TABLE IF NOT EXISTS "+LOCALITIES+" ("+
                LOCALITY+" TEXT NOT NULL,"+
                LOCALITY_ID+" INTEGER NOT NULL)";
        return sql;
    }

    public String getList(){
        String sql = "SELECT * FROM "+LOCALITIES;

        return sql;
    }

    public String deleteOldList(){
        String sql = "DELETE FROM "+LOCALITIES;

        return sql;
    }

    public String getLocalityId(String locality){
        String sql =  "SELECT "+LOCALITY_ID+" FROM "+LOCALITIES+" WHERE "+LOCALITY+" = '"+locality+"' LIMIT 1";
        return sql;
    }

    public String getLocalitiesTable(){
        return LOCALITIES;
    }

    public String getLocality(){
        return LOCALITY;
    }

    public String getLocalityId(){
        return LOCALITY_ID;
    }


    public String [] getLocalityArray(){
        return LOCALITY_ARRAY;
    }
}
