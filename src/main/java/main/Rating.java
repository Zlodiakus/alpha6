package main;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Shadilan
 */
public class Rating {

    private static String intToSpaceStr(int Int) {
        String tempStr="",tempStr2="",ret="";
        tempStr2=Integer.toString(Int);
        tempStr = new StringBuffer(tempStr2).reverse().toString();
        tempStr2="";
        while (tempStr.length()>3) {
            tempStr2=tempStr2.concat(tempStr.substring(0,3));
            tempStr2=tempStr2.concat(";psbn&");
            tempStr=tempStr.substring(3);
        }
        tempStr2=tempStr2.concat(tempStr);
        ret= new StringBuffer(tempStr2).reverse().toString();
        return ret;
    }

    public static String getFullRate(){
        Connection con = null;
        String ccolor, sExp="",sGold="",sProfit="",sAmbushed="",sPaladined="",tempStr="", tempStr2="",sChested="";
        int i=0;
        //String result="<table><tr><td>№</td><td>Имя</td><td>Уровень</td><td>Золото</td><td>Опыт</td><td>Корованов</td><td>Сумарная дистанция</td><td>Максимальная дистанция</td><td>Средняя дистанция</td></tr>";
        String result="";
        try {
            con=DBUtils.ConnectDB();
            //PreparedStatement stmt=con.prepareStatement("select  p.Name ,p.Level ,p.Gold ,p.Exp ,count(c.Distance) cor_count,sum(c.Distance) sum_distance,Max(c.Distance) max_distance,round(avg(c.Distance)) avg_distance, (select Name from Fractions where Id=p.Race) from Players p left join Caravans c on (p.GUID=c.PGUID and c.Finish is not null) WHERE p.Name !=  'Elf' group by p.Name,p.Level,p.Gold,p.Exp order by Exp desc");
            PreparedStatement stmt=con.prepareStatement("select  p.Name ,p.Level, p.Exp, (select Quantity from resources where PGUID=p.GUID and type='Gold') Gold, count(c.Distance) cor_count, p.Race, sum(c.profit) profit, s.ambushed, s.paladined, s.chested from Players p left join Caravans c on (p.GUID=c.PGUID and c.Finish is not null) left join Stats s on (p.GUID=s.PGUID) WHERE p.Name !=  'Elf' group by p.Name,p.Level,p.Exp, Gold order by Exp desc");
            ResultSet rs=stmt.executeQuery();
            rs.beforeFirst();
            while (rs.next()){
                i=i+1;
                ccolor="primary";
                if (rs.getInt(6)==1) {
                    ccolor="info";
                }
                if (rs.getInt(6)==2) {
                    ccolor="danger";
                }
                if (rs.getInt(6)==3) {
                    ccolor="warning";
                }
                sExp= intToSpaceStr(rs.getInt(3));
                sGold= intToSpaceStr(rs.getInt(4));
                sProfit= intToSpaceStr(rs.getInt(7));
                sAmbushed=intToSpaceStr(rs.getInt(8));
                sPaladined=intToSpaceStr(rs.getInt(9));
                sChested=intToSpaceStr(rs.getInt("chested"));
                result=result+"<tr class="+ccolor+"><td>"+i+"</td><td><b>"+rs.getString("Name")+"</b></td><td class=\"r\">"+rs.getInt("Level")+"</td><td class=\"r\">"+sExp+"</td><td class=\"r\">"+sGold+"</td><td class=\"r\">"+rs.getInt(5)+"</td><td class=\"r\">"+sProfit+"</td><td class=\"r\">"+sAmbushed+"</td><td class=\"r\">"+sPaladined+"</td><td class=\"r\">" + sChested + "</td></tr>";
            }
            //result=result+"</table>";
            con.close();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            try {
                if (con!=null && !con.isClosed()) con.close();
                e.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return result;

    }

    public static ArrayList<String[]> getFullRateArray(){
        Connection con = null;
        String ccolor, sExp="",sGold="",sProfit="",sAmbushed="",sPaladined="",tempStr="", tempStr2="",sChested="";
        int i=0;
        //String result="<table><tr><td>№</td><td>Имя</td><td>Уровень</td><td>Золото</td><td>Опыт</td><td>Корованов</td><td>Сумарная дистанция</td><td>Максимальная дистанция</td><td>Средняя дистанция</td></tr>";
//        String[][] result = new String[];
        String[] oneString=new String[11];
        ArrayList<String[]> Rate = new ArrayList<String[]>();
        try {
            con=DBUtils.ConnectDB();
            //PreparedStatement stmt=con.prepareStatement("select  p.Name ,p.Level ,p.Gold ,p.Exp ,count(c.Distance) cor_count,sum(c.Distance) sum_distance,Max(c.Distance) max_distance,round(avg(c.Distance)) avg_distance, (select Name from Fractions where Id=p.Race) from Players p left join Caravans c on (p.GUID=c.PGUID and c.Finish is not null) WHERE p.Name !=  'Elf' group by p.Name,p.Level,p.Gold,p.Exp order by Exp desc");
            PreparedStatement stmt=con.prepareStatement("select  p.Name ,p.Level, p.Exp, p.Gold, count(c.Distance) cor_count, p.Race, sum(c.profit) profit, s.ambushed, s.paladined, s.chested from Players p left join Caravans c on (p.GUID=c.PGUID and c.Finish is not null) left join Stats s on (p.GUID=s.PGUID) WHERE p.Name !=  'Elf' group by p.Name,p.Level,p.Gold,p.Exp order by Exp desc");
            ResultSet rs=stmt.executeQuery();
            rs.beforeFirst();
            while (rs.next()){
                i=i+1;
                ccolor="primary";
                if (rs.getInt(6)==1) {
                    ccolor="info";
                }
                if (rs.getInt(6)==2) {
                    ccolor="danger";
                }
                if (rs.getInt(6)==3) {
                    ccolor="warning";
                }
                sExp= intToSpaceStr(rs.getInt(3));
                sGold= intToSpaceStr(rs.getInt(4));
                sProfit= intToSpaceStr(rs.getInt(7));
                sAmbushed=intToSpaceStr(rs.getInt(8));
                sPaladined=intToSpaceStr(rs.getInt(9));
                sChested=intToSpaceStr(rs.getInt("chested"));
                oneString[0]=Integer.toString(i);
                oneString[1]=Integer.toString(rs.getInt("Race"));
                oneString[2]=rs.getString("Name");
                oneString[3]=Integer.toString(rs.getInt("Level"));
                oneString[4]=sExp;
                oneString[5]=sGold;
                oneString[6]=Integer.toString(rs.getInt("cor_count"));
                oneString[7]=sProfit;
                oneString[8]=sAmbushed;
                oneString[9]=sPaladined;
                oneString[10]=sChested;
                Rate.add(oneString);
                //result=result+"<tr class="+ccolor+"><td>"+i+"</td><td><b>"+rs.getString("Name")+"</b></td><td class=\"r\">"+rs.getInt("Level")+"</td><td class=\"r\">"+sExp+"</td><td class=\"r\">"+sGold+"</td><td class=\"r\">"+rs.getInt(5)+"</td><td class=\"r\">"+sProfit+"</td><td class=\"r\">"+sAmbushed+"</td><td class=\"r\">"+sPaladined+"</td><td class=\"r\">" + sChested + "</td></tr>";
            }
            //result=result+"</table>";
            con.close();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            try {
                if (con!=null && !con.isClosed()) con.close();
                e.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return Rate;

    }

}
