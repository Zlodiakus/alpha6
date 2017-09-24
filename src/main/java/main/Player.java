package main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.json.stream.JsonParser;
import javax.naming.NamingException;
import javax.naming.spi.DirStateFactory;
//import javax.resource.cci.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import main.MyUtils;

import static java.lang.System.in;
import static main.MyUtils.Logwrite;
import static main.MyUtils.isBetween;
import main.Params;

/**
 * Created by Well on 17.01.2016.
 * Player object
 */
public class Player {
    String LastError;
    String GUID = "";
    String Name = "";
    int Level = 0;
    int Exp = 0;
    int Race;
    int Gold = 0;
    int Obsidian=0;
    int Lat = 100;
    int Lng = 200;
    int Hirelings;
    int HirelingsInAmbushes;
    boolean tower;
    int maxQuantity=0;
    int currentQuantity=0;
    int manaRegen=80;
    int maxMana=2500;
    boolean flagLevelChanged=false;
    JSONObject jresult = new JSONObject();
    JSONArray jarr = new JSONArray();
    String result = "";
    Connection con = null;
    Runtime r = Runtime.getRuntime();


    public Player (String PGUID) {
        GUID=PGUID;
        try {
            con = DBUtils.ConnectDB();
        } catch (SQLException e) {
            result = MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        } catch (NamingException e) {
            result = MyUtils.getJSONError("ResourceError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
        if (!result.equals("")) result = "No access to DB: " + result;
    }
    public Player() {
        try {
            con = DBUtils.ConnectDB();
        } catch (SQLException e) {
            result = MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        } catch (NamingException e) {
            result = MyUtils.getJSONError("ResourceError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
        if (!result.equals("")) result = "No access to DB: " + result;
    }

    public Player(String PGUID, Connection CON) {
        PreparedStatement query;
        con = CON;
        GUID = PGUID;
        try {
            query = con.prepareStatement("select Name, Level, Exp, Race from Players where GUID=?");
            query.setString(1, GUID);
            ResultSet rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                Name = rs.getString("Name");
                Level = rs.getInt("Level");
                Exp = rs.getInt("Exp");
                //Gold = rs.getInt("Gold");
                //Obsidian = rs.getInt("Obsidian");
                Race = rs.getInt("Race");
                //Hirelings = rs.getInt("Hirelings");
            } else {
                MyUtils.Logwrite("Player","Player "+GUID+" not found");
            }

            Gold=readResource("Gold");
            Obsidian=readResource("Obsidian");
            Hirelings=readResource("Hirelings");

            query = con.prepareStatement("select 10*sum(Life) as HirelingsInAmbushes from Ambushes where PGUID=?");
            query.setString(1,GUID);
            rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                HirelingsInAmbushes = rs.getInt("HirelingsInAmbushes");
            }
            else {HirelingsInAmbushes=0;}

            query = con.prepareStatement("select 1 from towers where PGUID=?");
            query.setString(1,GUID);
            rs = query.executeQuery();
            if (rs.isBeforeFirst()) tower=true;
            else tower=false;
            query.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("Player",e.toString());
        }
    }

    public Player(String Token, int PLAT, int PLNG) {
        //TODO: handle OldLat,OldLng/Lat,Lng
        //MyUtils.Logwrite("Player","создаем игрока для "+Token);
        int OldLat, OldLng;
        PreparedStatement query;
        ResultSet rs;
        Lat = PLAT;
        Lng = PLNG;
        try {
            con = DBUtils.ConnectDB();
        } catch (SQLException e) {
            result = MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        } catch (NamingException e) {
            result = MyUtils.getJSONError("ResourceError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
        if (!result.equals("")) result = "No access to DB: " + result;

        try {
            query = con.prepareStatement("select z1.GUID, z1.Name, z1.Level, z1.Exp, z1.Race, z2.Lat, z2.Lng from Connections z0, Players z1, GameObjects z2 where z0.Token=? and z0.PGUID=z1.GUID and z2.GUID=z1.GUID");
            query.setString(1, Token);
            rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                GUID = rs.getString("GUID");
                Name = rs.getString("Name");
                Level = rs.getInt("Level");
                Exp = rs.getInt("Exp");
                //Gold = rs.getInt("Gold");
                //Obsidian = rs.getInt("Obsidian");
                Race = rs.getInt("Race");
                OldLat = rs.getInt("Lat");
                OldLng = rs.getInt("Lng");
                //Hirelings = rs.getInt("Hirelings");
                rs.close();
            } else {
                LastError = MyUtils.getJSONError("NOUSERFOUND", "(" + Token + ")");
            }
            Gold=readResource("Gold");
            Obsidian=readResource("Obsidian");
            Hirelings=readResource("Hirelings");

            PreparedStatement query2 = con.prepareStatement("select 10*sum(Life) as HirelingsInAmbushes from Ambushes where PGUID=?");
            query2.setString(1,GUID);
            ResultSet rs2 = query2.executeQuery();
            if (rs2.isBeforeFirst()) {
                rs2.first();
                HirelingsInAmbushes = rs2.getInt("HirelingsInAmbushes");
                //MyUtils.Logwrite("Player","HirelingsInAmbushes="+HirelingsInAmbushes+" GUID="+GUID);
            }
            else {HirelingsInAmbushes=0; //MyUtils.Logwrite("Player","else HirelingsInAmbushes=0. GUID="+GUID);
            }
            query = con.prepareStatement("select 1 from towers where PGUID=?");
            query.setString(1,GUID);
            rs = query.executeQuery();
            if (rs.isBeforeFirst()) tower=true;
            else tower=false;
            query.close();
        } catch (SQLException e) {
            LastError = MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public String getTowerGUID() {
        String towerGUID="";
        try {
        PreparedStatement query = con.prepareStatement("select GUID from towers where PGUID=?");
        query.setString(1,GUID);
        ResultSet rs = query.executeQuery();
        if (!rs.isBeforeFirst()) towerGUID="notower";
        else{
            rs.first();
            towerGUID=rs.getString("GUID");
        }
        query.close();
    } catch (SQLException e) {
        MyUtils.Logwrite("Player",e.toString());
    }
    return towerGUID;
    }

    public String setRace(int race) {
        if (!(race >= 1 && race <=3)) { jresult.put("Result","O1101");jresult.put("Message","Выбрана несуществующая фракция");MyUtils.Logwrite("Player.setRace",Name +" читер? выбрал несуществующую расу "+race);}
        else {
            Race = race;
            update();
        }
        return jresult.toString();
    }

    public void addStat(String columnC, int valueV) {

        MyUtils.Logwrite("Player.addStat","Start! "+Name+". column = "+columnC+". value = "+Integer.toString(valueV));
        PreparedStatement query;
        try {
            String tempStr="update Stats set " + columnC + " = " + columnC + " + ? where PGUID = ?";
            query=con.prepareStatement(tempStr);
            query.setInt(1,valueV);
            query.setString(2,GUID);
            //MyUtils.Logwrite("Player.addStat","tempStr = "+tempStr+", query="+query.toString());
            query.execute();
            con.commit();
            query.close();
            MyUtils.Logwrite("Player.addStat",Name+". column = "+columnC+". value = "+Integer.toString(valueV));
        } catch (SQLException e) {
            MyUtils.Logwrite("Player.addStat",Name+". Error "+e.toString());
        }
   }


//Сложна как-то потом с рейтингом будет работать, оставлю пока старую таблицу
    public void addStat2(String columnC, int valueV) {

        MyUtils.Logwrite("Player.addStat2","Start! "+Name+". column = "+columnC+". value = "+Integer.toString(valueV));
        PreparedStatement query;
        try {
            query=con.prepareStatement("INSERT INTO Stats2 (PGUID,Type,Value) VALUES (?,?,?) ON DUPLICATE KEY UPDATE Value=?");
            query.setString(1,GUID);
            query.setString(2,columnC);
            query.setInt(3,valueV);
            query.setInt(4,valueV);

            //MyUtils.Logwrite("Player.addStat","tempStr = "+tempStr+", query="+query.toString());
            query.execute();
            con.commit();
            query.close();
            MyUtils.Logwrite("Player.addStat2",Name+". column = "+columnC+". value = "+Integer.toString(valueV));
        } catch (SQLException e) {
            MyUtils.Logwrite("Player.addStat2",Name+". Error "+e.toString());
        }

    }

    private boolean createPlayerResources() {
        PreparedStatement query;
        try {
            query=con.prepareStatement("insert into resources (select ?,type,0 from resourceTypes)");
            query.execute();
            query.close();
            return true;
        } catch (SQLException e)
        {
            Logwrite("createPlayerRes","SQL ERROR: "+e.toString());
            return false;
        }
    }


    public String register(String Login, String Password) {
        PreparedStatement query;
        try {
            query=con.prepareStatement("select GUID from Users where Login=? and Password=?");
            query.setString(1,Login);
            query.setString(2,Password);
            ResultSet rs = query.executeQuery();
            rs.first();
            GUID=rs.getString("GUID");
            rs.close();
            query=con.prepareStatement("insert into Players (GUID, Name, Level, Exp, Class, Race) values (?,?,1,0,0,0)");
            query.setString(1,GUID);
            query.setString(2,Login);
            query.execute();
//            writeResource("Gold",0);
//            writeResource("Hirelings",100);
//            writeResource("Obsidian",0);
            query=con.prepareStatement("insert into Stats (PGUID) values (?)");
            query.setString(1,GUID);
            query.execute();
            query=con.prepareStatement("insert into GameObjects (GUID, Lat, Lng, Type) values (?,100,100,'Player')");
            query.setString(1,GUID);
            query.execute();
            query.close();
            if (generateStartUpgrades() && generateStartResources()) {
                addResource("Hirelings",100);
                con.commit();
                return Login + " успешно зарегистрирован!";}
            else {con.rollback(); return Login + ", при регистрации возникли проблемы! Повторите попытку или обратитесь к администратору.";}
        } catch (SQLException e)
        {
            try {con.rollback();}
            catch (SQLException e1) {
                //какие-то траблы с базой, хз, ничего не делаем
            }
            return "SQL Error while registration: "+e.toString();
        }

    }

    public String userRegister(String Login, String Password, String EMail, String Invite) {
        PreparedStatement query;
        String UGUID=UUID.randomUUID().toString();
        String ret;
        try {
            query=con.prepareStatement("select count(1) from Invites where used=0 and Invite=?");
            query.setString(1,Invite);
            ResultSet rs=query.executeQuery();
            rs.first();
            if (rs.getInt(1)>0) {
                query=con.prepareStatement("update Invites set used=1 where Invite=?");
                query.setString(1,Invite);
                query.execute();
                query = con.prepareStatement("insert into Users (GUID, Login, Password, email) values (?,?,?,?)");
                query.setString(1, UGUID);
                query.setString(2, Login.trim());
                query.setString(3, Password.trim());
                query.setString(4, EMail.trim());
                query.execute();
                con.commit();
                ret=register(Login, Password);
            } else {ret="Try another invite, this one don't work!";}
            rs.close();
            query.close();
            con.close();
            return ret;
        } catch (SQLException e)
        {
            return "SQL Error while registration: "+e.toString();
        }
    }

    public String getInvite(String Login, String Password) {
        MyUtils.Logwrite("getInvite","Запрошен инвайт");
        PreparedStatement query;
        String Invite,ret;
        try {
            query=con.prepareStatement("select Invite from Invites where given=0 and used=0");
            ResultSet rs=query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                Invite=rs.getString("Invite");
                query=con.prepareStatement("update Invites set given=1 where Invite=?");
                query.setString(1,Invite);
                query.execute();
                con.commit();
                ret=Invite;
            } else {ret="No more ungiven invites!";}
            query.close();
            rs.close();
        } catch(SQLException e) {ret=e.toString();}
        return ret;
    }

    public boolean generateStartResources() {
        PreparedStatement query;
        try {
            query = con.prepareStatement("insert into resources(PGUID,type,quantity) (select ?,type,0 from resourceTypes)");
            query.setString(1,GUID);
            query.execute();
            return true;
        } catch (SQLException e) { MyUtils.Logwrite("Player.genStartRes","SQL Error: "+e.toString());return false;}
    }

    public boolean generateStartUpgrades() {
        PreparedStatement query;
        String UGUID;
        try {
            query = con.prepareStatement("select GUID from Upgrades WHERE level=0");
            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                    UGUID=rs.getString("GUID");
                    query = con.prepareStatement("insert into PUpgrades(PGUID,UGUID) values(?,?) ");
                    query.setString(1, GUID);
                    query.setString(2, UGUID);
                    query.execute();
                }
            query.close();
            rs.close();
            return true;
        } catch (SQLException e) { MyUtils.Logwrite("generateStartUpgrades","Error: "+e.toString());return false; }
    }

/* Переходим к "награде за действия", разделяем опыт и деньги
    public void getGold(int GOLD) {
        Exp += GOLD;
        Gold += GOLD;
        if (checkForLevel()) {
            Level += 1;
        }
        update();
        //Fraction fraction = new Fraction(Race,con);
        //fraction.getGold(GOLD,con);
    }
*/


/* Неактуально, будет использоваться getResource("obsidian",quantity)
    public void getObsidian(int OBSIDIAN) {

        Obsidian += OBSIDIAN;
        update();
    }
*/

    public void getGold(int GOLD) {
        addResource("gold",GOLD);
//временная затычка, коммит должен быть не здесь, потом надо будет определить
        try {
            con.commit();
        }
        catch (SQLException e)
        {
            //ЖОПА
        }
    }

    public void getExp(int EXP) {
        Exp+=EXP;
        if (checkForLevel()) {
            Level += 1;
            flagLevelChanged=true;
        }
        update();
    }


    private boolean checkForLevel() {
        if (Level<20) return (Exp>=getTNL());
        else return false;
    }


    public void update() {
        PreparedStatement query;
        try {
            query = con.prepareStatement("update Players set Level=?, Exp=?, Race=?, manaRegen=?, maxMana=? where GUID=?");
            query.setInt(1,Level);
            query.setInt(2,Exp);
            query.setInt(3,Race);
            query.setInt(4,manaRegen);
            query.setInt(5,maxMana);
            query.setString(6,GUID);
            query.execute();
            con.commit();
            query.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("Player.update","Failed player "+Name+" update. SQL Error: "+e.toString());
        }
    }

    public void UpdatePUpgrades(String UGUIDold, String UGUIDnew) {
        PreparedStatement query;
        try {
            query = con.prepareStatement("update PUpgrades set UGUID=? where PGUID=? and UGUID=?");
            query.setString(1, UGUIDnew);
            query.setString(2, GUID);
            query.setString(3,UGUIDold);
            query.execute();
            con.commit();
            query.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("UpdatePUpgrades","Failed. Player "+Name+" UGUID old = "+UGUIDold+" UGUID new = "+UGUIDnew+". SQL Error: "+e.toString());
        }
    }

    public void bonusUpgradeRecount(float koef) {
        PreparedStatement query;
        try {
            query = con.prepareStatement("update Caravans set bonus=bonus*?, profit=profit*? where PGUID=?");
            query.setFloat(1, koef);
            query.setFloat(2, koef);
            query.setString(3,GUID);
            query.execute();
            con.commit();
            query.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("bonusUpgradeRecount","Failed. Player "+Name+". SQL Error: "+e.toString());
        }
    }

    public void profitUpgradeRecount(int newacc, int newspeed) {
        PreparedStatement query;
        try {
            query = con.prepareStatement("Update Caravans z1 set z1.profit=60*z1.bonus/((?-1-?)/?+(z1.Distance-((1+?)*(?-1-?)/?+POW((?-1-?)/?,2)*?/2))/?) WHERE PGUID = ?");
            query.setInt(1, newspeed);
            query.setInt(2, newacc);
            query.setInt(3, newacc);
            query.setInt(4, newacc);
            query.setInt(5, newspeed);
            query.setInt(6, newacc);
            query.setInt(7, newacc);
            query.setInt(8, newspeed);
            query.setInt(9, newacc);
            query.setInt(10, newacc);
            query.setInt(11, newacc);
            query.setInt(12, newspeed);
            query.setString(13,GUID);
            query.execute();
            con.commit();
            query.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("profitUpgradeRecount","Failed. Player "+Name+". SQL Error: "+e.toString());
        }
    }
    private float getKoefByOtherUps(String upType)
    {
        PreparedStatement query;
        try {
            query = con.prepareStatement("select sum(z1.Level) from Upgrades z1, PUpgrades z2 where z2.PGUID=? and z1.GUID=z2.UGUID and z1.Type not like ?");
            query.setString(1, GUID);
            query.setString(2, upType);
            ResultSet rs = query.executeQuery();
            rs.first();
            return 1+(float)rs.getInt(1)/10;
        } catch (SQLException e) {
            MyUtils.Logwrite("getKoefByOtherUps","Failed. Player "+Name+". SQL Error: "+e.toString());
            return 1;
        }
    }


    public String BuyUpgrade(String CGUID) {
        MyUtils.Logwrite("BuyUpgrade","Started by "+Name, r.freeMemory());
        String ret;
        String flag="nothing";
        int upcost;
        float RaceDiscount, RaceBonus;
        if (checkRangeToObj(CGUID)) {
            City city = new City(CGUID, con);
            Upgrade currentUpgrade = new Upgrade(GUID, CGUID, con);
            if (currentUpgrade.GUID.equals("0")) {
                jresult.put("Result","BD001");
                jresult.put("Message", "Ошибка обращения к БД");
                //jresult.put("Error", "Техническая ошибка 20001, обратитесь в службу моральной поддержки!");
                MyUtils.Logwrite("BuyUpgrade","Can't create object currentUpgrade " + currentUpgrade.result);
                return jresult.toString();
            }
            if (currentUpgrade.Level == 20)
            {
                jresult.put("Result","O0706");
                jresult.put("Message", "Достигнут максимальный уровень апгрейда.");
                return jresult.toString();
            }
            Upgrade targetUpgrade = new Upgrade(currentUpgrade.Type, currentUpgrade.Level + 1, con);
            if (targetUpgrade.GUID.equals("0")) {
                MyUtils.Logwrite("BuyUpgrade","Can't create object targetUpgrade " + targetUpgrade.result);
                jresult.put("Result","BD001");
                jresult.put("Message", "Ошибка обращения к БД");
                //jresult.put("Error", "Техническая ошибка 20002, обратитесь в службу моральной поддержки!");
                return jresult.toString();
            }
            if (Level >= targetUpgrade.ReqPlayerLev) {
                if (city.Level >= targetUpgrade.ReqCityLev) {
                    RaceBonus=0;
                    int totalInfluence=city.Influence1+city.Influence2+city.Influence3;
                    float conc = 0;
                    if (totalInfluence>0) {
                        int maxInfluence = Math.max(Math.max(city.Influence1,city.Influence2),city.Influence3);
                        conc= (float)(3*maxInfluence - totalInfluence)/(4*totalInfluence);
                        flag="sum inf ok.";
                        if (Race==1) {RaceBonus=(float)city.Influence1/totalInfluence;flag=flag+" race1 ok.";}
                        if (Race==2) {RaceBonus=(float)city.Influence2/totalInfluence;flag=flag+" race2 ok.";}
                        if (Race==3) {RaceBonus=(float)city.Influence3/totalInfluence;flag=flag+" race3 ok.";}
                    }
                    RaceDiscount=1-RaceBonus/4;
                    float otherUpsEffect=getKoefByOtherUps(targetUpgrade.Type);
                    upcost=(int)(otherUpsEffect*(1+conc)*(targetUpgrade.Cost*RaceDiscount*(100-getPlayerUpgradeEffect1("bargain"))/100));
                    MyUtils.Logwrite("Player.BuyUpgrade","Player="+Name+", Race="+Race+", other Ups Effect = "+otherUpsEffect+", conc = "+conc+", Inf1="+city.Influence1+",Inf2="+city.Influence2+",Inf3="+city.Influence3+", RaceBonus="+Float.toString(RaceBonus)+", RaceDiscount="+Float.toString(RaceDiscount)+", upcost="+upcost+", flag="+flag);
                    if (payResources("Gold",upcost)) {
                        Gold -= upcost;
                        UpdatePUpgrades(currentUpgrade.GUID, targetUpgrade.GUID);
                        update();
                        if (targetUpgrade.Type.equals("cargo")) {bonusUpgradeRecount((float)targetUpgrade.Effect1/currentUpgrade.Effect1);}
                        if (targetUpgrade.Type.equals("bargain")) {bonusUpgradeRecount((float)(100+targetUpgrade.Effect2)/(100+currentUpgrade.Effect2));}
                        if (targetUpgrade.Type.equals("speed")) {profitUpgradeRecount(targetUpgrade.Effect1,targetUpgrade.Effect2);}
                        city.getGold((int)(upcost/(5*(1+conc)*(1+conc))));
                        //targetUpgrade.update(GUID, con);
                        jresult.put("Result","OK");
                        JSONObject jobj = new JSONObject();
                        jobj.put("Type", targetUpgrade.Type);
                        jobj.put("Name", targetUpgrade.Name);
                        jobj.put("Description", targetUpgrade.Description);
                        jobj.put("Level", targetUpgrade.Level);
                        jobj.put("Effect1",targetUpgrade.Effect1);
                        jobj.put("Effect2",targetUpgrade.Effect2);
                        jresult.put("Upgrade",jobj);

                        Upgrade nextUpgrade = new Upgrade(targetUpgrade.Type, targetUpgrade.Level + 1, con);
                        if (nextUpgrade.GUID.equals("0")) {
                            MyUtils.Logwrite("BuyUpgrade","Can't create object nextUpgrade " + nextUpgrade.result);
                            //TODO - лажа полная, надо как-то переделать, низя 2 резалта выдавать.
                            jresult.put("Result","BD001");
                            jresult.put("Message", "Ошибка обращения к БД");
                            //jresult.put("Error", "Техническая ошибка 20002, обратитесь в службу моральной поддержки!");
                            return jresult.toString();
                        }
                        else {
                            rollback(con);
                            jobj = new JSONObject();
                            jobj.put("Type", nextUpgrade.Type);
                            jobj.put("Name", nextUpgrade.Name);
                            jobj.put("Description", nextUpgrade.Description);
                            jobj.put("Level", nextUpgrade.Level);
                            jobj.put("ReqCityLev", nextUpgrade.ReqCityLev);
                            jobj.put("Cost", (int) (nextUpgrade.Cost*getKoefByOtherUps(nextUpgrade.Type)));
                            jresult.put("NextUpgrade", jobj);
                        }

                        ret = jresult.toString();
                    } else {
                        jresult.put("Result","O0703");
                        jresult.put("Message", "Вам не хватает золота на покупку этого умения");
                        ret = jresult.toString();
                    }
                } else {
                    jresult.put("Result","O0704");
                    jresult.put("Message", "Этот город слишком мал, в нем никто не может обучить умению "+targetUpgrade.Level+" уровня. Требуемый уровень города - "+targetUpgrade.ReqCityLev);
                    ret = jresult.toString();
                }
            } else {
                jresult.put("Result","O0705");
                jresult.put("Message", "Ваш уровень слишком мал для приобретения этого умения!");
                ret = jresult.toString();
            }
        } else {
            jresult.put("Result","O0702");
            jresult.put("Message", "Город слишком далеко.");
            ret = jresult.toString();
        }
        MyUtils.Logwrite("BuyUpgrade","Finished by "+Name, r.freeMemory());
        return ret;
    }

    private int getTNL() {
        PreparedStatement query;
        int TNL;
        try {
            query = con.prepareStatement("select exp from Levels where Type='player' and level=?");
            query.setInt(1, Level + 1);
            ResultSet rs = query.executeQuery();
            rs.first();
            TNL = rs.getInt(1);
            rs.close();
            query.close();
        } catch (SQLException e) {TNL=Exp+1;}
        return TNL;
    }
//************************************ Def - GetRoutes
    public String GetRoutes(){
        MyUtils.Logwrite("GetRoutes","Started by "+Name, r.freeMemory());
        int Caravans, Distance,LatS,LngS,LatF,LngF,profit;
        String CarGUID, StartGUID, StartName,FinishGUID,FinishName;
        ResultSet rs;
        JSONArray jarr2;
        if (GUID.equals("")) {jresult.put("Result","DB001");jresult.put("Message","Player not found."); return jresult.toString();}
        PreparedStatement query;
        try {
            query = con.prepareStatement("select count(1) from Caravans where Finish is not null and PGUID=?");
            query.setString(1, GUID);
            rs = query.executeQuery();
            rs.first();
            Caravans = rs.getInt(1);
            rs.close();
            jresult.put("Caravans", Caravans);
            jarr2 = new JSONArray();
            //query=con.prepareStatement("select z1.GUID,z1.Start,(select Name from Cities z2 where z2.GUID=z1.Start) StartName,z1.Finish,(select Name from Cities z2 where z2.GUID=z1.Finish) FinishName,z1.Distance from Caravans z1 where PGUID=?");
            query=con.prepareStatement("select z1.GUID,z2.Lat,z2.Lng,z1.Start,(select Name from Cities z2 where z2.GUID=z1.Start) StartName,(select Lat from GameObjects where GUID=z1.Start) as LatS,(select Lng from GameObjects where GUID=z1.Start) as LngS,z1.Finish,(select Name from Cities z2 where z2.GUID=z1.Finish) FinishName,(select Lat from GameObjects where GUID=z1.Finish) as LatF,(select Lng from GameObjects where GUID=z1.Finish) as LngF,z1.Distance, z1.profit from Caravans z1 left join GameObjects z2 on (z1.GUID=z2.GUID) where z1.PGUID=?");
            query.setString(1, GUID);
            rs = query.executeQuery();
            while (rs.next()) {
                JSONObject jobj = new JSONObject();
                CarGUID=rs.getString("GUID");
                StartGUID=rs.getString("Start");
                StartName=rs.getString("StartName");
                FinishGUID=rs.getString("Finish");
                FinishName=rs.getString("FinishName");
                Distance=rs.getInt("Distance");
                profit=rs.getInt("profit");
                LatS=rs.getInt("LatS");
                LngS=rs.getInt("LngS");
                LatF=rs.getInt("LatF");
                LngF=rs.getInt("LngF");
                jobj.put("GUID", CarGUID);
                jobj.put("Lat",rs.getInt("Lat"));
                jobj.put("Lng",rs.getInt("Lng"));
                jobj.put("StartGUID", StartGUID);
                jobj.put("StartName", StartName);
                jobj.put("FinishGUID", FinishGUID);
                jobj.put("FinishName", FinishName);
                jobj.put("Distance", Distance);
                jobj.put("profit", profit);
                jobj.put("StartLat",LatS);
                jobj.put("StartLng",LngS);
                jobj.put("FinishLat",LatF);
                jobj.put("FinishLng",LngF);
                jarr2.add(jobj);
            }
            rs.close();
            query.close();
            jresult.put("Routes",jarr2);
            jresult.put("Result", "OK");
        }
     catch (SQLException e) {
        jresult.put("Result","DB001");
        jresult.put("Message","Ошибка обращения к БД");
    }
        MyUtils.Logwrite("GetRoutes","Finished by "+Name, r.freeMemory());
        return jresult.toString();
    }
//****************************************
    public String GetPlayerInfo() {
        MyUtils.Logwrite("GetPlayerInfo","Started by "+Name, r.freeMemory());
        int TNL, Caravans,Ambushes,AmbushesMax,AmbushesLeft,UpLevel,Distance,AmbLat,AmbLng,AmbushRadius,ActionDistance,LatS,LngS,LatF,LngF,UpCost,UpReqCityLevel, profit,foundedCities;
        String UpType,UpName,UpDesc,CarGUID,StartGUID,StartName,FinishGUID,FinishName,AmbGUID,AmbName;
        ResultSet rs;
        JSONArray jarr2 = new JSONArray();
        if (GUID.equals("")) {jresult.put("Result","DB001");jresult.put("Message","Player not found."); return jresult.toString();}

        jresult.put("GUID",GUID);
        jresult.put("PlayerName",Name);
        jresult.put("Level",Level);
        jresult.put("Exp",Exp);
        jresult.put("Gold",Gold);
        jresult.put("Obsidian",Obsidian);
        jresult.put("Race",Race);
        jresult.put("Hirelings",Hirelings);

        int citiesFounded=foundedCities();
        int goldToNextCity=Math.max(0,(citiesFounded-10)* 10000);
        int obsidianToNextCity=Math.max(0,(citiesFounded-10));

        JSONObject jobj;
        JSONArray jarr;
        jarr=new JSONArray();
        jobj=new JSONObject();
        jobj.put("Type","Gold");
        jobj.put("Quantity",goldToNextCity);
        jarr.add(jobj);

        jobj=new JSONObject();
        jobj.put("Type","Obsidian");
        jobj.put("Quantity",obsidianToNextCity);
        jarr.add(jobj);

        jresult.put("nextCityCost",jarr);

        jarr=new JSONArray();
        jobj = new JSONObject();

        //Кривой код, потенциально могу тут передавать notower в GUID'е
        if (tower) jresult.put("Tower", getTowerGUID());
        //int LeftToHire=getPlayerUpgradeEffect1("leadership") - Hirelings - HirelingsInAmbushes;
        int LeftToHire=95+Level*(100+5*Level) - Hirelings - HirelingsInAmbushes;
        jresult.put("LeftToHire",LeftToHire);
        //jresult.put("HirelingsInAmbushes",HirelingsInAmbushes);
        PreparedStatement query;
        try {
            if (Level<20) TNL = getTNL() - Exp;
            else TNL=0;
            jresult.put("TNL",TNL);
            query=con.prepareStatement("select count(1) from Caravans where Finish is not null and PGUID=?");
            query.setString(1, GUID);
            rs = query.executeQuery();
            rs.first();
            Caravans = rs.getInt(1);
            rs.close();
            jresult.put("Caravans",Caravans);
            query=con.prepareStatement("select count(1) from Cities where Creator=? and kvant=0");
            query.setString(1,GUID);
            rs=query.executeQuery();
            if (rs.first()) foundedCities=rs.getInt(1);
            else foundedCities=0;
            jresult.put("FoundedCities",foundedCities);

            query=con.prepareStatement("select count(1) from Ambushes where PGUID=?");
            query.setString(1, GUID);
            rs = query.executeQuery();
            rs.first();
            Ambushes = rs.getInt(1);
            rs.close();

            AmbushesMax=getPlayerUpgradeEffect1("set_ambushes");
            AmbushesLeft=AmbushesMax-Ambushes;
            jresult.put("AmbushesMax",AmbushesMax);
            jresult.put("AmbushesLeft",AmbushesLeft);
            jresult.put("MostIn",0);
            AmbushRadius=getPlayerUpgradeEffect1("ambushes");
            jresult.put("AmbushRadius",AmbushRadius);
            ActionDistance=getPlayerUpgradeEffect1("paladin");
            jresult.put("ActionDistance",ActionDistance);
            query=con.prepareStatement("select z1.Type,z1.Name,z1.Description,z1.Level, z1.Effect1,z1.Effect2 from Upgrades z1, PUpgrades z2 where z2.PGUID=? and z2.UGUID=z1.GUID order by z1.Level desc");
            query.setString(1, GUID);
            rs = query.executeQuery();
            while (rs.next()) {
                jobj = new JSONObject();
                UpType=rs.getString("Type");
                UpName=rs.getString("Name");
                UpDesc=rs.getString("Description");
                UpLevel=rs.getInt("Level");
                jobj.put("Type", UpType);
                jobj.put("Name", UpName);
                jobj.put("Description", UpDesc);
                jobj.put("Level", UpLevel);
                jobj.put("Effect1",rs.getInt("Effect1"));
                jobj.put("Effect2",rs.getInt("Effect2"));
                jarr.add(jobj);
            }
            rs.close();
            jresult.put("Upgrades",jarr);

            query=con.prepareStatement("select z3.Type,z3.Name,z3.ChangedDesc as Description,z3.Level, z3.ReqCityLev, z3.Cost from Upgrades z3 where (z3.Type,z3.Level) in (select z1.Type,z1.Level+1 from Upgrades z1, PUpgrades z2 where z2.PGUID=? and z2.UGUID=z1.GUID)");
            query.setString(1, GUID);
            rs = query.executeQuery();
            while (rs.next()) {
                jobj = new JSONObject();
                UpType=rs.getString("Type");
                UpName=rs.getString("Name");
                UpDesc=rs.getString("Description");
                UpLevel=rs.getInt("Level");
                UpCost=rs.getInt("Cost");
                UpReqCityLevel=rs.getInt("ReqCityLev");
                jobj.put("Type", UpType);
                jobj.put("Name", UpName);
                jobj.put("Description", UpDesc);
                jobj.put("Level", UpLevel);
                jobj.put("ReqCityLev", UpReqCityLevel);
                jobj.put("Cost",(int)(UpCost*getKoefByOtherUps(UpType)));
                jobj.put("OUC",getKoefByOtherUps(UpType));
                jarr2.add(jobj);
            }
            rs.close();
            jresult.put("NextUpgrades",jarr2);


            jarr2 = new JSONArray();
            //query=con.prepareStatement("select z1.GUID,z1.Start,(select Name from Cities z2 where z2.GUID=z1.Start) StartName,z1.Finish,(select Name from Cities z2 where z2.GUID=z1.Finish) FinishName,z1.Distance from Caravans z1 where PGUID=?");
            query=con.prepareStatement("select z1.GUID,z2.Lat,z2.Lng,z1.Start,(select Name from Cities z2 where z2.GUID=z1.Start) StartName,(select Lat from GameObjects where GUID=z1.Start) as LatS,(select Lng from GameObjects where GUID=z1.Start) as LngS,z1.Finish,(select Name from Cities z2 where z2.GUID=z1.Finish) FinishName,(select Lat from GameObjects where GUID=z1.Finish) as LatF,(select Lng from GameObjects where GUID=z1.Finish) as LngF,z1.Distance, z1.profit from Caravans z1 left join GameObjects z2 on (z1.GUID=z2.GUID) where z1.PGUID=?");
            query.setString(1, GUID);
            rs = query.executeQuery();
            while (rs.next()) {
                jobj = new JSONObject();
                CarGUID=rs.getString("GUID");
                StartGUID=rs.getString("Start");
                StartName=rs.getString("StartName");
                FinishGUID=rs.getString("Finish");
                FinishName=rs.getString("FinishName");
                Distance=rs.getInt("Distance");
                profit=rs.getInt("profit");
                LatS=rs.getInt("LatS");
                LngS=rs.getInt("LngS");
                LatF=rs.getInt("LatF");
                LngF=rs.getInt("LngF");
                jobj.put("GUID", CarGUID);
                jobj.put("Lat",rs.getInt("Lat"));
                jobj.put("Lng",rs.getInt("Lng"));
                jobj.put("StartGUID", StartGUID);
                jobj.put("StartName", StartName);
                jobj.put("FinishGUID", FinishGUID);
                jobj.put("FinishName", FinishName);
                jobj.put("Distance", Distance);
                jobj.put("profit", profit);
                jobj.put("StartLat",LatS);
                jobj.put("StartLng",LngS);
                jobj.put("FinishLat",LatF);
                jobj.put("FinishLng",LngF);
                jarr2.add(jobj);
            }
            rs.close();
            jresult.put("Routes",jarr2);

            jarr2 = new JSONArray();
            query=con.prepareStatement("select z1.GUID,z1.Lat,z1.Lng,z2.Name,z2.Radius,z2.TTS,z2.Life from GameObjects z1, Ambushes z2 where z2.GUID=z1.GUID and z2.PGUID=?");
            query.setString(1, GUID);
            rs = query.executeQuery();
            while (rs.next()) {
                jobj = new JSONObject();
                AmbGUID=rs.getString("GUID");
                AmbLat=rs.getInt("Lat");
                AmbLng=rs.getInt("Lng");
                AmbName="Засада "+rs.getString("Name");
                jobj.put("Type","Ambush");
                jobj.put("GUID", AmbGUID);
                jobj.put("Lat", AmbLat);
                jobj.put("Lng", AmbLng);
                jobj.put("Owner",0);
                jobj.put("Radius",rs.getInt("Radius"));
                jobj.put("Ready",rs.getInt("TTS"));
                jobj.put("Life",rs.getInt("Life")*10);
                jobj.put("Name", AmbName);
                jarr2.add(jobj);
            }
            rs.close();
            query.close();
            jresult.put("Ambushes",jarr2);

            //surveys
            jarr2 = new JSONArray();
            query=con.prepareStatement("select GUID,Lat,Lng,type,maxQuantity,currentQuantity,maxQuantity2,currentQuantity2,maxQuantity3,currentQuantity3 from surveys where PGUID=?");
            query.setString(1, GUID);
            rs = query.executeQuery();
            while (rs.next()) {
                jobj = new JSONObject();
                jobj.put("Type","Survey");
                jobj.put("GUID", rs.getString("GUID"));
                jobj.put("Lat", rs.getInt("Lat"));
                jobj.put("Lng", rs.getInt("Lng"));
                    JSONArray jarr3 = new JSONArray();
                    JSONObject jobj2 = new JSONObject();
                    jobj2.put("Type",rs.getString("type"));
                    jobj2.put("Prob",rs.getString("currentQuantity"));
                    jobj2.put("maxProb",rs.getString("maxQuantity"));
                    jarr3.add(jobj2);
                    jobj2 = new JSONObject();
                    String type2="",type3="";
                    if (rs.getString("type").equals("stone")) {
                        type2="obsidian";
                        type3="iron";
                    }
                    if (rs.getString("type").equals("wood")) {
                        type2="amber";
                        type3="redwood";
                    }
                    if (rs.getString("type").equals("grain")) {
                        type2="hop";
                        type3="wool";
                    }
                    jobj2.put("Type",type2);
                    jobj2.put("Prob",rs.getString("currentQuantity2"));
                    jobj2.put("maxProb",rs.getString("maxQuantity2"));
                    jarr3.add(jobj2);

                    jobj2 = new JSONObject();
                    jobj2.put("Type",type3);
                    jobj2.put("Prob",rs.getString("currentQuantity3"));
                    jobj2.put("maxProb",rs.getString("maxQuantity3"));
                    jarr3.add(jobj2);

                jobj.put("Survey",jarr3);
                jarr2.add(jobj);
            }
            rs.close();
            query.close();
            jresult.put("Surveys",jarr2);


        } catch (SQLException e) {
            jresult.put("Result","DB001");
            jresult.put("Message","Ошибка обращения к БД");
        }
        MyUtils.Logwrite("GetPlayerInfo","Finished by "+Name, r.freeMemory());
        return jresult.toString();
    }

    public boolean CheckAmbushesQuantity() {
        //select z1.Effect1-(select count(1) from Ambushes where PGUID=?) from Upgrades z1, PUpgrades z2 where z2.UGUID=z1.GUID and z2.PGUID=? and z1.TYPE='set_ambushes'
        //if (result>0) return true
        //else return false
        PreparedStatement query;
        ResultSet rs;
        int AmbLeft;
        String result;
        boolean ret;
        try {
            query = con.prepareStatement("select z1.Effect1-(select count(1) from Ambushes where PGUID=?) from Upgrades z1, PUpgrades z2 where z2.UGUID=z1.GUID and z2.PGUID=? and z1.TYPE='set_ambushes'");
            query.setString(1, GUID);
            query.setString(2, GUID);
            rs = query.executeQuery();
            rs.first();
            AmbLeft = rs.getInt(1);
            rs.close();
            query.close();
            ret = (AmbLeft > 0);
        } catch (SQLException e) {
            ret = false;
        }
        return ret;
    }

    public boolean CheckCitiesQuantity() {
        //select z1.Effect1-(select count(1) from Ambushes where PGUID=?) from Upgrades z1, PUpgrades z2 where z2.UGUID=z1.GUID and z2.PGUID=? and z1.TYPE='set_ambushes'
        //if (result>0) return true
        //else return false
        PreparedStatement query;
        ResultSet rs;
        int CitLeft;
        String result;
        boolean ret;
        try {
            query = con.prepareStatement("select z1.Effect1-(select count(1) from Cities where Creator=? and kvant=0) from Upgrades z1, PUpgrades z2 where z2.UGUID=z1.GUID and z2.PGUID=? and z1.TYPE='founder'");
            query.setString(1, GUID);
            query.setString(2, GUID);
            rs = query.executeQuery();
            rs.first();
            CitLeft = rs.getInt(1);
            rs.close();
            query.close();
            ret = (CitLeft > 0);
        } catch (SQLException e) {
            ret = false;
        }
        return ret;
    }

    private String fastScan() {
        PreparedStatement query;
        ResultSet rs;
        int radius = getPlayerUpgradeEffect1("paladin");
        try {
            query = con.prepareStatement("select z1.GUID, z1.Lat, z1.Lng, z1.Type from GameObjects z1 where z1.Type not like 'City' and ?>=round(6378137 * acos(cos(z1.Lat / 1e6 * PI() / 180) * cos(? / 1e6 * PI() / 180) * cos(z1.Lng / 1e6 * PI() / 180 - ? / 1e6 * PI() / 180) + sin(z1.Lat / 1e6 * PI() / 180) * sin(? / 1e6 * PI() / 180)))");
            query.setInt(1,radius+65);
            query.setInt(2,Lat);
            query.setInt(3,Lng);
            query.setInt(4,Lat);
            rs = query.executeQuery();
            while (rs.next()) {
                JSONObject jobj = new JSONObject();
                jobj.put("GUID", rs.getString("GUID"));
                jobj.put("Lat", rs.getInt("Lat"));
                jobj.put("Lng", rs.getInt("Lng"));
                jobj.put("Type", rs.getString("Type"));
                jarr.add(jobj);
            }
            jresult.put("FastScan",jarr);
        } catch (SQLException e) {
            jresult.put("Result", "DB001");
            jresult.put("Message","Ошибка обращения к БД");
            MyUtils.Logwrite("fastScan","Error: "+e.toString());
        }
        return jresult.toString();
    }

    public void drinkAway(int drinkBonus) {
        MyUtils.Logwrite("drinkAway","Start");
        PreparedStatement query,query2;
        ResultSet rs,rs2;
        int totalLevel,citiesCount;
        try {
            query2=con.prepareStatement("select count(1), sum(Level) from Cities where Creator like ?");
            query2.setString(1,GUID);
            rs2=query2.executeQuery();
            rs2.first();
            citiesCount=rs2.getInt(1);
            totalLevel=rs2.getInt(2);

            query = con.prepareStatement("select GUID, Level, Name from Cities where Creator like ?");
            query.setString(1,GUID);
            rs = query.executeQuery();
            while (rs.next()) {
                City city=new City(rs.getString("GUID"),con);
                int bonus=(int)(0.2*drinkBonus*rs.getInt("Level")*citiesCount/totalLevel);
                MyUtils.Logwrite("drinkAway","Пропиваем "+bonus+" золота в "+rs.getString("Name"));
                city.getGold(bonus,Race);
            }
        } catch (SQLException e) {
            MyUtils.Logwrite("drinkAway","Error: "+e.toString());
        }
        MyUtils.Logwrite("drinkAway","Finish");
    }

    public String ScanRange() {
        Random random = new Random();

        PreparedStatement query, query2;
        ResultSet rs;
        String TPGUID, CName, CUpgradeType, Start, Finish, StartName, FinishName, CUName, TName, CreatorName;
        int CLevel, TRadius, TTTS, CRadius, StartLat, StartLng, FinishLat, FinishLng, Speed, progress,COwner,AOwner, TLife,CHirelings;
        MyUtils.Logwrite("ScanRange","Started by "+Name,r.freeMemory());
        String TGUID, Type, TLat, TLng, Result;
        long CExp, NExp, TExp, Inf1, Inf2, Inf3;
        //int deltaLatCities=1000000*Math.asin((180/Math.PI)*(X km)/(6378137))
        //int deltaLatCities=17967; //2km
        int deltaLatCities=14374; //1.6km
        int deltaLngCities=(int)(deltaLatCities/Math.cos((Lat / 1e6) * Math.PI / 180));
        if (GUID.equals("")) {jresult.put("Error","No player found."); return jresult.toString();}
        try {
            //Караваны
            query = con.prepareStatement("select z1.GUID, z1.Lat, z1.Lng, z1.Type, z2.PGUID, z2.Start, z2.Finish, z2.Speed, z3.Race from GameObjects z1, Caravans z2, Players z3 where z3.GUID=z2.PGUID and z2.GUID=z1.GUID and ? between z1.Lat-12000 and z1.Lat+12000 and ? between z1.Lng-15000 and z1.Lng+15000");
            query.setInt(1, Lat);
            query.setInt(2, Lng);

            rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    JSONObject jobj = new JSONObject();
                    TGUID = rs.getString("GUID");
                    Type = rs.getString("Type");
                    TLat = rs.getString("Lat");
                    TLng = rs.getString("Lng");
                    TPGUID = rs.getString("PGUID");
                    Start = rs.getString("Start");
                    Finish = rs.getString("Finish");
                    Speed = rs.getInt("Speed");
                    if (TPGUID.equals(GUID))
                    {
                        COwner=0;
                        jobj.put("GUID", TGUID);
                        jobj.put("Type", Type);
                        jobj.put("Lat", TLat);
                        jobj.put("Lng", TLng);
                        jobj.put("Owner", COwner);
                    }
                    else {
                        COwner = rs.getInt("Race");
                        //1-апрельские шутки
                        //COwner+=1;if (COwner==4) COwner=1;}
                        //else COwner=random.nextInt(3)+1;
                        jobj.put("GUID", TGUID);
                        jobj.put("Type", Type);
                        //if (!Name.equals("Shadilan")&&!Name.equals("Zlodiak")) {
                        //    jobj.put("Lat", TLat);
                        //    jobj.put("Lng", TLng);
                        //}
                        jobj.put("Owner", COwner);
                    }

                    query2 = con.prepareStatement("select z2.Name StartName,z1.Lat StartLat,z1.Lng StartLng,z4.Name FinishName,z3.Lat FinishLat,z3.Lng FinishLng from GameObjects z1, Cities z2, GameObjects z3, Cities z4 where z2.GUID=z1.GUID and z4.GUID=z3.GUID and z1.GUID=? and z3.GUID=?");
                    query2.setString(1, Start);
                    query2.setString(2, Finish);
                    ResultSet rs2=query2.executeQuery();
                    rs2.first();
                    StartName=rs2.getString("StartName");
                    StartLat=rs2.getInt("StartLat");
                    StartLng=rs2.getInt("StartLng");
                    FinishName=rs2.getString("FinishName");
                    FinishLat=rs2.getInt("FinishLat");
                    FinishLng=rs2.getInt("FinishLng");
                    jobj.put("StartName", StartName);
                    jobj.put("StartLat", StartLat);
                    jobj.put("StartLng", StartLng);
                    jobj.put("FinishName", FinishName);
                    jobj.put("FinishLat", FinishLat);
                    jobj.put("FinishLng", FinishLng);
                    jobj.put("Speed", Speed);
                    jarr.add(jobj);
                }
            }

            //Засады
            query = con.prepareStatement("select z1.GUID, z1.Lat, z1.Lng, z1.Type,z2.PGUID,z2.Radius,z2.TTS,z2.Name,z3.Race,z2.Life from GameObjects z1, Ambushes z2, Players z3 where z3.GUID=z2.PGUID and z2.GUID=z1.GUID and ? between z1.Lat-12000 and z1.Lat+12000 and ? between z1.Lng-15000 and z1.Lng+15000");
            query.setInt(1, Lat);
            query.setInt(2, Lng);
            rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    JSONObject jobj = new JSONObject();
                    TGUID = rs.getString("GUID");
                    Type = rs.getString("Type");
                    TLat = rs.getString("Lat");
                    TLng = rs.getString("Lng");
                    TPGUID = rs.getString("PGUID");
                    TRadius = rs.getInt("Radius");
                    TTTS = rs.getInt("TTS");
                    TName = rs.getString("Name");
                    TLife=rs.getInt("Life");
                    if (TPGUID.equals(GUID))
                    {
                        AOwner=0;
                        jobj.put("GUID", TGUID);
                        jobj.put("Type", Type);
                        jobj.put("Lat", TLat);
                        jobj.put("Lng", TLng);
                        jobj.put("Owner", AOwner);
                        jobj.put("Radius", TRadius);
                        jobj.put("Ready", TTTS);
                        jobj.put("Name",TName);
                        jobj.put("Life",TLife*10);
                        jarr.add(jobj);
                    }

                    else
                    {
                        AOwner=rs.getInt("Race");
                        jobj.put("GUID", TGUID);
                        jobj.put("Type", Type);
                        //if (!Name.equals("Shadilan")&&!Name.equals("Zlodiak")) {
                        //    jobj.put("Lat", TLat);
                        //    jobj.put("Lng", TLng);
                        //}
                        jobj.put("Owner", AOwner);
                        jobj.put("Radius", TRadius);
                        jobj.put("Ready", TTTS);
                        jobj.put("Name",TName);
                        jobj.put("Life",TLife*10);
                        jarr.add(jobj);
                    }
                }
            }

            //Башни
            query=con.prepareStatement("select z1.GUID,z1.Lat,z1.Lng,z1.Type,z2.name,z2.text,z2.level,z2.obsidian, (select z3.Race from Players z3 where z3.GUID=z2.PGUID) Race from GameObjects z1,towers z2 where z1.GUID=z2.GUID and z1.Type='Tower' and ? between z1.Lat-? and z1.Lat+? and ? between z1.Lng-? and z1.Lng+?");
            query.setInt(1, Lat);
            query.setInt(2, deltaLatCities);
            query.setInt(3, deltaLatCities);
            query.setInt(4, Lng);
            query.setInt(5, deltaLngCities);
            query.setInt(6, deltaLngCities);
            rs = query.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    JSONObject jobj = new JSONObject();
                    jobj.put("GUID", rs.getString("GUID"));
                    jobj.put("Type", rs.getString("Type"));
                    jobj.put("Lat", rs.getString("Lat"));
                    jobj.put("Lng", rs.getString("Lng"));
                    jobj.put("Name", rs.getString("name"));
                    jobj.put("Level", rs.getInt("level"));
                    jobj.put("Text",rs.getString("text"));
                    jobj.put("Race",rs.getInt("Race"));
                    JSONArray jitems = new JSONArray();
                    JSONObject jitem = new JSONObject();
                        jitem.put("Type","Obsidian");
                        jitem.put("Weight",0);
                        jitem.put("Quantity",rs.getInt("obsidian"));
                        jitems.add(jitem);
                    jobj.put("Storage",jitems);
                    jarr.add(jobj);
                }
            }

            //Города
            query = con.prepareStatement("select z1.GUID, z1.Lat, z1.Lng, z1.Type,z2.Hirelings, z2.Creator, (select Name from Players p where z2.Creator=p.GUID) as CreatorName, (select Name from Cities c where z2.Creator=c.GUID) as CityCreatorName, z2.Name,z2.Level,z2.Exp currentExp,z3.Exp nextLevelExp,z4.Exp thisLevelExp, z2.UpgradeType, (select z3.Name from Upgrades z3 where z2.UpgradeType=z3.Type and z3.Level=0 LIMIT 1) UName, z2.Influence1, z2.Influence2, z2.Influence3 from GameObjects z1 USE INDEX (`LatLng`), Cities z2, Levels z3, Levels z4 where z2.GUID=z1.GUID and ? between z1.Lat-? and z1.Lat+? and ? between z1.Lng-? and z1.Lng+? and z3.Type='city' and z3.Level=z2.Level+1 and z4.level=z2.level and z4.Type='City'");
            query.setInt(1, Lat);
            query.setInt(2, deltaLatCities);
            query.setInt(3, deltaLatCities);
            query.setInt(4, Lng);
            query.setInt(5, deltaLngCities);
            query.setInt(6, deltaLngCities);
            rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    JSONObject jobj = new JSONObject();
                    TGUID = rs.getString("GUID");
                    Type = rs.getString("Type");
                    TLat = rs.getString("Lat");
                    TLng = rs.getString("Lng");
                    CName = rs.getString("Name");
                    CLevel = rs.getInt("Level");
                    CExp = rs.getLong("currentExp");
                    NExp = rs.getLong("nextLevelExp");
                    TExp = rs.getLong("thisLevelExp");
                    progress=(int)(100*(CExp-TExp)/(NExp-TExp));
                    CUpgradeType = rs.getString("UpgradeType");
                    CUName = rs.getString("UName");
                    Inf1 = rs.getLong("Influence1");
                    Inf2 = rs.getLong("Influence2");
                    Inf3 = rs.getLong("Influence3");
                    //CRadius=50+2*(CLevel - 1);
                    CRadius=50;
                    CreatorName=(rs.getString("CreatorName") == null)?rs.getString("CityCreatorName"):rs.getString("CreatorName");
                    CHirelings=rs.getInt("Hirelings");
                    jobj.put("GUID", TGUID);
                    jobj.put("Type", Type);
                    jobj.put("Lat", TLat);
                    jobj.put("Lng", TLng);
                    jobj.put("Name", CName);
                    jobj.put("Level", CLevel);
                    jobj.put("Progress",progress);
                    jobj.put("UpgradeType",CUpgradeType);
                    jobj.put("UpgradeName",CUName);
                    jobj.put("Radius",CRadius);
                    jobj.put("Influence1",Inf1);
                    jobj.put("Influence2",Inf2);
                    jobj.put("Influence3",Inf3);
                    jobj.put("Owner",rs.getString("Creator").equals(GUID));
                    jobj.put("Creator",CreatorName);
                    jobj.put("Hirelings",CHirelings);
                    jarr.add(jobj);
                }
            }
            jresult.put("Objects", jarr);
            rs.close();
            query.close();
        } catch (SQLException e) {
            jresult.put("Error", "ScanRange. "+e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
        MyUtils.Logwrite("ScanRange","Finished by "+Name, r.freeMemory());
        return jresult.toString();
    }

    public static double getRadius(String PGUID, Connection con) {
        int result;
        PreparedStatement query;
        try {
            query = con.prepareStatement("select z3.Effect1 from Players z1, PUpgrades z2, Upgrades z3 where z1.GUID=? and z2.PGUID=z1.GUID and z2.UGUID=z3.GUID and z3.Type='paladin'");
            query.setString(1, PGUID);
            ResultSet rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                result = rs.getInt(1);
            } else {
                result = 0;
            }
            rs.close();
            query.close();
            return result;
        } catch (SQLException e) {
            return 0; //pizdec. esli polomaetsia zdes - hyi naidesh )
        }
    }

    public double getRadius() {
        int result;
        PreparedStatement query;
        try {
            query = con.prepareStatement("select z3.Effect1 from Players z1, PUpgrades z2, Upgrades z3 where z1.GUID=? and z2.PGUID=z1.GUID and z2.UGUID=z3.GUID and z3.Type='paladin'");
            query.setString(1, GUID);
            ResultSet rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                result = rs.getInt(1);
            } else {
                result = 0;
            }
            rs.close();
            query.close();
            return result;
        } catch (SQLException e) {
            return 0; //pizdec. esli polomaetsia zdes - hyi naidesh )
        }
    }

    public String GetGUIDByToken(Connection con, String Token) throws SQLException {
        PreparedStatement query;
        query = con.prepareStatement("select PGUID from Connection where Token=? limit 1");
        query.setString(1, Token);
        ResultSet rs = query.executeQuery();
        if (rs.isBeforeFirst()) {
            rs.first();
            GUID = rs.getString("PGUID");
            rs.close();
            query.close();
            return GUID;
        } else {
            query.close();
            LastError = "Error: NOUSERFOUND (" + Token + ")";
            return LastError;
        }
    }

    public boolean checkRangeToObj(String TGUID) {
        int lat, lng;
        PreparedStatement query;
        try {
            query=con.prepareStatement("select Lat,Lng from GameObjects where GUID=?");
            query.setString(1,TGUID);
            ResultSet rs = query.executeQuery();
            rs.first();
            lat = rs.getInt(1);
            lng = rs.getInt(2);
            rs.close();
            query.close();
        } catch (SQLException e) {return false;}
        return (MyUtils.RangeCheck(Lat, Lng, lat, lng) <= getRadius());
    }

    public String getUnfinishedRoute() {
        String RGUID;
        PreparedStatement query;
        try {
            query=con.prepareStatement("select GUID from Caravans where PGUID=? and Finish is null");
            query.setString(1,GUID);
            ResultSet rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                RGUID = rs.getString("GUID");
                rs.close();
            }
            else {RGUID="No route";}
            query.close();
            return RGUID;
        } catch (SQLException e) {return "Error";}
    }

    public String getGUID() {
        return GUID;
    }

    public String getLastError() {
        return LastError;
    }

    public String StartRoute(String TGUID) {
        MyUtils.Logwrite("StartRoute","Started by "+Name, r.freeMemory());
        String res;
        String checkUnfinishedRoute;
        if (checkRangeToObj(TGUID)) {
            checkUnfinishedRoute=getUnfinishedRoute();
            if (checkUnfinishedRoute.equals("No route")) {
                Caravan caravan=new Caravan(con);
                res=caravan.StartRoute(GUID, TGUID, con);
            }
            else {
                if (checkUnfinishedRoute.equals("Error")) {
                    jresult.put("Result", "DB001");
                    jresult.put("Message", "Ошибка обращения к БД");
                    res=jresult.toString();
                }
                else {
                    jresult.put("Result", "O0503");
                    jresult.put("Message", "У тебя уже есть незавершенный маршрут!");
                    res=jresult.toString();
                }
            }
        } else {
            jresult.put("Result", "O0502");
            jresult.put("Message", "Город слишком далеко.");
            res=jresult.toString();
        }
        MyUtils.Logwrite("StartRoute","Finished by "+Name, r.freeMemory());
        return res;
    }

    public String SetAmbush(int TLAT, int TLNG) {
        int TTS, Radius, Life;
        String res;
        MyUtils.Logwrite("SetAmbush","Started by "+Name, r.freeMemory());
        if (MyUtils.RangeCheck(Lat, Lng, TLAT, TLNG) <= getRadius()) {
            if (CheckAmbushesQuantity()) {
                TTS=getPlayerUpgradeEffect2("set_ambushes");
                Radius=getPlayerUpgradeEffect1("ambushes");
                Life=1;//getPlayerUpgradeEffect2("ambushes"); - отменяем старый эффект апгрейда
                if (!payResources("Hirelings",10)) {rollback(con);jresult.put("Result", "O0204");jresult.put("Message","Вам не хватает наемников для установки засады!");res=jresult.toString();}
                else {
                    Ambush ambush = new Ambush();
                    res = ambush.Set(GUID, TLAT, TLNG, Radius, -TTS, Life, false, con);
                    if (res.contains("OK")) {Hirelings-=10*Life;update();}
                }
            } else {
                jresult.put("Result", "O0203");jresult.put("Message","Все засады уже установлены!");
                res=jresult.toString();
            }
        } else {
            jresult.put("Result", "O0202"); jresult.put("Message", "Засада слишком далеко!");
            res=jresult.toString();
        }
        MyUtils.Logwrite("SetAmbush","Finished by "+Name, r.freeMemory());
        return res;
    }

    public int getBounty() {
        PreparedStatement query;
        try {
            query=con.prepareStatement("select value from params where name='bounty'");
            ResultSet rs = query.executeQuery();
            rs.first();
            return rs.getInt("value");
        }
        catch (SQLException e) {MyUtils.Logwrite("Ambush.getBounty","Error: "+e.toString());return 0;}
    }

    public String DestroyAmbush(String TGUID) {
        MyUtils.Logwrite("DestroyAmbush", "Started by " + Name, r.freeMemory());
        String res;
        int bonus;
        int actionExp, actionGold;
        JSONObject jobj = new JSONObject();
        Ambush ambush = new Ambush(TGUID, con);
        if (ambush.Race == Race) {
            jresult.put("Result", "O0303");
            //jresult.put("Message", "Нельзя уничтожать засады своей фракции!");
            res = jresult.toString();
        } else {
            if (MyUtils.RangeCheck(ambush.Lat, ambush.Lng, Lat, Lng) <= getRadius()) {
                if (Hirelings < 5 * ambush.Life) {
                    jresult.put("Result", "O0304");
                    //jresult.put("Message", "Вам не хватает наемников для уничтожения засады!");
                    res = jresult.toString();
                } else {
                    res = ambush.Destroy(TGUID, con);
                    jobj.put("Result", "OK");
                    if (res.equals(jobj.toString())) {
                        //bonus = 10 + getPlayerUpgradeEffect2("paladin");
                        bonus = (int) (((20 + Math.min(720, ambush.TTS + 180) * ambush.Life) * getPlayerUpgradeEffect2("paladin") * (1+(float)(getPlayerUpgradeEffect2("bargain")/100)))*(100+getBounty()) / 2000);
                        actionExp=0;
                        if (ambush.PGUID.equals("Elf")) {bonus+=1000;actionExp=1500;}
                        //jobj.put("Message", "Награда за уничтожение засады составила " + Integer.toString(bonus) + " золота!");
                        actionGold=bonus;
                        actionExp+=(20 + Math.min(720, ambush.TTS + 180))*5;
                        actionExp*=2;
                        actionExp=(actionExp*(100+getEffect("exp")))/100;
                        jobj.put("Exp",actionExp);
                        jobj.put("Gold",actionGold);
                        //TODO payResources не вычитает значения из соответствующей переменной плеера. либо убирать эти переменные из плеера, либо уменьшать их параллельно
                        Hirelings -= 5;// * ambush.Life; //апдейт в гетГолде пройдет
                        payResources("Hirelings",5);
                        getGold(actionGold);
                        getExp(actionExp);
                        jobj.put("Level",Level);
                        jobj.put("isLevelChanged",flagLevelChanged);
                        //TODO переделать статистику
                        addStat("paladined", bonus);
                        addStat("paladinedExp", actionExp);
                        addStat("Npaladins", 1);
                        MyUtils.Message(ambush.PGUID, "Ваша засада " + ambush.Name + " была уничтожена!", 2, 0, ambush.Lat, ambush.Lng);
                        res = jobj.toString();
                    }
                }
            } else {
                jresult.put("Result", "O0302");
                //jresult.put("Message", "Засада слишком далеко!");
                res = jresult.toString();
            }
        }
        MyUtils.Logwrite("DestroyAmbush", "Finished by " + Name, r.freeMemory());
        return res;
    }

    public String CancelAmbush(String TGUID) {
        MyUtils.Logwrite("CancelAmbush","Started by "+Name, r.freeMemory());
        PreparedStatement query;
        String res, OwnerGUID="";
        JSONObject jobj=new JSONObject();
        try {
            query = con.prepareStatement("select PGUID from Ambushes where GUID=?");
            query.setString(1, TGUID);
            ResultSet rs = query.executeQuery();
            if (rs.isBeforeFirst()){
            rs.first();
            OwnerGUID = rs.getString("PGUID");}
            else {jresult.put("Result","O0401");jresult.put("Message","Засада не найдена.");return jresult.toString();}
            rs.close();
        }
        catch (SQLException e) {jresult.put("Result","DB001");jresult.put("Message","Ошибка обращения к БД.");return jresult.toString();}
        if (GUID.equals(OwnerGUID)) {
            Ambush ambush=new Ambush(TGUID,con);
            res=ambush.Destroy(TGUID,con);
            jobj.put("Result","OK");
            if (res.equals(jobj.toString())) {
                Hirelings+=10;
                addResource("Hirelings",10);
                update();
                MyUtils.Logwrite("Player.CancelAmbush","Ambush "+TGUID+" canceled by owner "+GUID);
            }
        } else {
            jresult.put("Message", "You are not owner, cheater!");
            res=jresult.toString();
        }
        MyUtils.Logwrite("CancelAmbush","Started by "+Name, r.freeMemory());
        return res;
    }


    public int getPlayerUpgradeEffect1(String Type) {
        int res;
        PreparedStatement query;
        try {
            query = con.prepareStatement("select z1.Effect1 from Upgrades z1, PUpgrades z2 where z2.UGUID=z1.GUID and z2.PGUID=? and z1.type=?");
            query.setString(1,GUID);
            query.setString(2,Type);
            ResultSet rs=query.executeQuery();
            rs.first();
            res=rs.getInt(1);
            rs.close();
            query.close();
        } catch (SQLException e) {return 0;}
        return res;
    }

    public int getPlayerUpgradeEffect2(String Type) {
        int res;
        PreparedStatement query;
        try {
            query = con.prepareStatement("select z1.Effect2 from Upgrades z1, PUpgrades z2 where z2.UGUID=z1.GUID and z2.PGUID=? and z1.type=?");
            query.setString(1,GUID);
            query.setString(2,Type);
            ResultSet rs=query.executeQuery();
            rs.first();
            res=rs.getInt(1);
            rs.close();
            query.close();
        } catch (SQLException e) {return 0;}
        return res;
    }

    private void commit(Connection CON) {
        try {
            CON.commit();
        }
        catch (SQLException e) {Logwrite("commit", "SQL Error: "+e.toString());}
    }

    private void rollback(Connection CON) {
        try {
            CON.rollback();
        }
        catch (SQLException e) {Logwrite("rollback", "SQL Error: "+e.toString());}
    }

    public String FinishRoute(String TGUID) {
        int actionGold,actionExp;
        MyUtils.Logwrite("FinishRoute","Started by "+Name, r.freeMemory());
        String res,SGUID;
        String checkUnfinishedRoute,RGUID;
        if (checkRangeToObj(TGUID)) {
            checkUnfinishedRoute=getUnfinishedRoute();
            if (checkUnfinishedRoute.equals("No route")) {
                jresult.put("Result","O0603");
                jresult.put("Message", "Сначала начните маршрут!");
                res=jresult.toString();
            }
            else {
                if (checkUnfinishedRoute.equals("Error")) {
                    jresult.put("Result","BD001");
                    jresult.put("Message", "Ошибка обращения к БД");
                    res=jresult.toString();
                }
                else {
                    RGUID=checkUnfinishedRoute;
                    if (!doubleRoute(TGUID,RGUID)) {
                        int accel = getPlayerUpgradeEffect1("speed");
                        int speed = getPlayerUpgradeEffect2("speed");
                        int cargo = getPlayerUpgradeEffect1("cargo");
                        int cargo2 = getPlayerUpgradeEffect2("cargo");
                        int trade = getPlayerUpgradeEffect2("bargain");
                        City cityF = new City (TGUID, con);
                        try {
                            PreparedStatement query = con.prepareStatement("select Start from Caravans where Finish is null and PGUID=?");
                            query.setString(1, GUID);
                            ResultSet rs0 = query.executeQuery();
                            rs0.first();
                            SGUID = rs0.getString("Start");
                        }
                        catch (SQLException e) {
                            jresult.put("Result", "BD001");
                            jresult.put("Message", "Ошибка обращения к БД");
                            res = jresult.toString();
                            return res;
                        }
                        City cityS=new City(SGUID,con);

                        if (Hirelings<cityS.Level+cityF.Level) {jresult.put("Result","O0606");jresult.put("Message","Недостаточно людей для запуска каравана. Нужно "+(cityS.Level+cityF.Level));res=jresult.toString();}
                        else {
                            Caravan caravan = new Caravan(con);
                            jresult = caravan.FinishRoute(RGUID, TGUID, speed, accel, cargo, trade, con);
                            if (jresult.toString().contains("OK")) {
                                Hirelings-=cityS.Level+cityF.Level;
                                payResources("Hirelings",cityS.Level+cityF.Level);
                                //actionGold=(int)Math.floor((50 + cargo2) * (100+(float)trade)/100);
                                //actionExp=50 + cargo2;
                                actionGold=0;
                                actionExp=1000+10*getEffect("exp"); //1000*(1+getEffect("exp")/100);
                                jresult.put("Exp",actionExp);
                                jresult.put("Gold",actionGold);
                                getGold(actionGold);
                                getExp(actionExp);
                                jresult.put("Level",Level);
                                jresult.put("isLevelChanged",flagLevelChanged);
                                update();
                                //TODO Переделатть это убожество, надо нормально код отрефакторить
                                commit(con);
                                //TODO Переделать работу со статистикой
                                addStat("Ncaravans", 1);
                            }
                            res=jresult.toString();
                        }
                    }
                    else {
                        jresult.put("Result","O0604");
                        jresult.put("Message","Такой маршрут уже существует, вы не можете создать два одинаковых маршрута!");
                        res=jresult.toString();
                    }
                }
            }
        }
        else {
            jresult.put("Result","O0602");
            jresult.put("Message", "Город слишком далеко.");
            res=jresult.toString();
        }
        MyUtils.Logwrite("FinishRoute","Finished by "+Name, r.freeMemory());
        return res;
    }

    public String FinishStartRoute(String TGUID) {
        JSONArray FRjarr = new JSONArray();
        boolean flag=false;
        String restmp="";
        String mestmp="";
        MyUtils.Logwrite("FinishStartRoute","Started by "+Name, r.freeMemory());
        String res,SGUID;
        String checkUnfinishedRoute,RGUID;
        if (checkRangeToObj(TGUID)) {
            checkUnfinishedRoute=getUnfinishedRoute();
            if (checkUnfinishedRoute.equals("No route")) {
                //jresult.put("Result","O0603");
                //jresult.put("Message", "Завершение маршрута не удалось, т.к. маршрут не был стартован");
                res=jresult.toString();
                flag=true;
                restmp="3";
                mestmp="Завершение маршрута не удалось, т.к. маршрут не был стартован.";
            }
            else {
                if (checkUnfinishedRoute.equals("Error")) {
                    jresult.put("Result","BD001");
                    jresult.put("Message", "Ошибка обращения к БД");
                    res=jresult.toString();
                    flag=false;
                }
                else {
                    RGUID=checkUnfinishedRoute;
                    if (!doubleRoute(TGUID,RGUID)) {
                        int accel = getPlayerUpgradeEffect1("speed");
                        int speed = getPlayerUpgradeEffect2("speed");
                        int cargo = getPlayerUpgradeEffect1("cargo");
                        int cargo2 = getPlayerUpgradeEffect2("cargo");
                        int trade = getPlayerUpgradeEffect2("bargain");
                        City cityF = new City (TGUID, con);
                        try {
                            PreparedStatement query = con.prepareStatement("select Start from Caravans where Finish is null and PGUID=?");
                            query.setString(1, GUID);
                            ResultSet rs0 = query.executeQuery();
                            rs0.first();
                            SGUID = rs0.getString("Start");
                        }
                        catch (SQLException e) {
                            jresult.put("Result", "BD001");
                            jresult.put("Message", "Ошибка обращения к БД");
                            res = jresult.toString();
                            return res;
                        }
                        City cityS=new City(SGUID,con);

                        if (!payResources("Hirelings",cityS.Level+cityF.Level)) {jresult.put("Result","O0606");jresult.put("Message","Недостаточно людей для запуска каравана. Нужно "+(cityS.Level+cityF.Level));res=jresult.toString();flag=false;}
                        else {
                            Hirelings-=cityS.Level+cityF.Level;
                            //Caravan caravan = new Caravan(con);
                            //res = caravan.FinishRoute(RGUID, TGUID, speed, accel, cargo, con);
                            String CGUID=TGUID;
                            //-----------------------------------------------------------------------
                            //public String FinishRoute(String RGUID, String CGUID, int speed, int accel, int cargo, Connection con) {
                                JSONObject FRjobj = new JSONObject();
                                PreparedStatement FRquery;
                                int FRlevelS,FRlevelF;
                                double t,t1,t2,s1;
                                try{
                                    FRquery=con.prepareStatement("select Lat,Lng from GameObjects where GUID=?");
                                    FRquery.setString(1,CGUID);
                                    ResultSet FRrs=FRquery.executeQuery();
                                    FRrs.first();
                                    int FRLat=FRrs.getInt(1);
                                    int FRLng=FRrs.getInt(2);
                                    //rs.close();
                                    FRquery=con.prepareStatement("select z1.Lat,z1.Lng,z2.Start from GameObjects z1, Caravans z2 where z2.GUID=? and z2.Start=z1.GUID");
                                    FRquery.setString(1,RGUID);
                                    FRrs=FRquery.executeQuery();
                                    FRrs.first();
                                    int FRLatS=FRrs.getInt("Lat");
                                    int FRLngS=FRrs.getInt("Lng");
                                    String FRStart=FRrs.getString("Start");

                                    if ((FRLatS==FRLat) && (FRLngS==FRLng)) {jresult.put("Result","O0605");jresult.put("Message","Ваш маршрут начинается в этом городе, вы не можете завершить маршрут в нем!");return jresult.toString();}
                                    FRquery=con.prepareStatement("insert into GameObjects (GUID,Lat,Lng,Type) values (?,?,?,'Caravan')");
                                    FRquery.setString(1,RGUID);
                                    FRquery.setInt(2,FRLatS);
                                    FRquery.setInt(3,FRLngS);
                                    FRquery.execute();

                                    FRquery=con.prepareStatement("select Level, Name from Cities where GUID=?");
                                    FRquery.setString(1,FRStart);
                                    FRrs=FRquery.executeQuery();
                                    FRrs.first();
                                    FRlevelS=FRrs.getInt("Level");
                                    String FRStartName=FRrs.getString("Name");

                                    FRquery=con.prepareStatement("select Level, Name from Cities where GUID=?");
                                    FRquery.setString(1,CGUID);
                                    FRrs=FRquery.executeQuery();
                                    FRrs.first();
                                    FRlevelF=FRrs.getInt("Level");
                                    String FRFinishName=FRrs.getString("Name");

                                    int FRDistance=(int)MyUtils.distVincenty(FRLatS,FRLngS,FRLat,FRLng);
                                    //int FRbonus=(int)((Math.sqrt(FRlevelS*FRlevelF)*FRDistance*cargo)/1000);
                                    int FRbonus=(int) ((1+(float)FRlevelS/20)*(1+(float)FRlevelF/20)*((float)FRDistance*cargo/1000)*(1+(float)trade/100));

                                    t1=(double)(speed-1-accel)/accel;
                                    s1=(double)((1+accel)*t1+accel*t1*t1/2);
                                    t2=(double)(FRDistance-s1)/speed;
                                    t=t1+t2;
                                    int FRprofit=(int)(60*FRbonus/t);
                                    FRquery=con.prepareStatement("update Caravans set Finish=?,Speed=1,Distance=?, bonus=?,profit=?, Lifetime=0 where GUID=?");
                                    FRquery.setString(1,CGUID);
                                    FRquery.setInt(2,FRDistance);
                                    FRquery.setInt(3,FRbonus);
                                    FRquery.setInt(4,FRprofit);
                                    FRquery.setString(5,RGUID);
                                    FRquery.execute();
                                    //con.commit();
                                    FRquery.close();
                                    restmp ="0";
                                    FRjobj.put("GUID", RGUID);
                                    FRjobj.put("Lat",FRLat);
                                    FRjobj.put("Lng",FRLng);
                                    FRjobj.put("StartGUID", FRStart);
                                    FRjobj.put("StartName", FRStartName);
                                    FRjobj.put("FinishGUID", CGUID);
                                    FRjobj.put("FinishName", FRFinishName);
                                    FRjobj.put("Distance", FRDistance);
                                    FRjobj.put("profit", FRprofit);
                                    FRjobj.put("StartLat",FRLatS);
                                    FRjobj.put("StartLng",FRLngS);
                                    FRjobj.put("FinishLat",FRLat);
                                    FRjobj.put("FinishLng",FRLng);
                                    FRjarr.add(FRjobj);
                                    //Hirelings-=cityS.Level+cityF.Level;update();
                                    flag=true;
                                    //int actionGold=(int)Math.floor((50 + cargo2) * (100+(float)trade)/100);
                                    //int actionExp=50 + cargo2;
                                    int actionGold=0;
                                    int actionExp=1000+10*getEffect("exp"); //1000*(1+getEffect("exp")/100);
                                    jresult.put("Exp",actionExp);
                                    jresult.put("Gold",actionGold);
                                    getGold(actionGold);
                                    getExp(actionExp);
                                    jresult.put("Level",Level);
                                    jresult.put("isLevelChanged",flagLevelChanged);
                                    update();
                                    //TODO Переделатть это убожество, надо нормально код отрефакторить
                                    commit(con);
                                    //TODO Переделать работу со статистикой
                                    addStat("Ncaravans", 1);

                                } catch (SQLException e) {MyUtils.Logwrite("Caravan.FinishStartRoute","finish"+e.toString());jresult.put("Result","BD001");jresult.put("Message","Ошибка обращения к БД"); /*return jresult.toString();*/}
                            //    return jresult.toString();
                            //}
                            //-----------------------------------------------------------------------
                        }
                    }
                    else {
                        jresult.put("Result","O0604");
                        jresult.put("Message","Такой маршрут уже существует, вы не можете создать два одинаковых маршрута!");
                        res=jresult.toString();
                        flag=false;
                    }
                }
            }
        }
        else {
            jresult.put("Result","O0602");
            jresult.put("Message", "Город слишком далеко.");
            res=jresult.toString();
            flag=false;
        }
        if (flag) {
            //Caravan caravan = new Caravan(con);
            //String res2 = caravan.StartRoute(GUID, TGUID, con);
            //--------------------------------------------------------------------
            //public String StartRoute(String PGUID, String CGUID, Connection con) {
            String PGUID=GUID;
            String CGUID=TGUID;
            String SRGUID= UUID.randomUUID().toString();
                JSONObject jobj = new JSONObject();
                PreparedStatement SRquery;
                City city = new City(CGUID,con);
                try{
                    SRquery=con.prepareStatement("insert into Caravans (GUID,PGUID,Start,Speed) values (?,?,?,?) ");
                    SRquery.setString(1,SRGUID);
                    SRquery.setString(2,PGUID);
                    SRquery.setString(3,CGUID);
                    SRquery.setInt(4, 0);
                    SRquery.execute();
                    con.commit();
                    SRquery=con.prepareStatement("select Lat,Lng from GameObjects where GUID=?");
                    SRquery.setString(1,CGUID);
                    ResultSet SRrs=SRquery.executeQuery();
                    SRrs.first();
                    int SRLat=SRrs.getInt(1);
                    int SRLng=SRrs.getInt(2);
                    SRquery.close();
                    if (restmp.equals("0")) {jresult.put("Result","OK");}
                    else {jresult.put("Result","O060"+restmp); jresult.put("Message",mestmp);}
                    jobj.put("GUID",SRGUID);
                    jobj.put("StartLat",SRLat);
                    jobj.put("StartLng",SRLng);
                    jobj.put("StartGUID",CGUID);
                    jobj.put("StartName",city.Name);
                    FRjarr.add(jobj);
                } catch (SQLException e) {jresult.put("Result","DB01"+restmp);jresult.put("Message",mestmp+"Ошибка обращения к БД при создании маршрута.");/*return jresult.toString();*/}
                //jresult.put("Route",jobj);
            jresult.put("Routes",FRjarr);
            //    return jresult.toString();
            //}

            //--------------------------------------------------------------------
        }

        MyUtils.Logwrite("FinishStartRoute","Finished by "+Name, r.freeMemory());
        return jresult.toString();
    }

    private boolean doubleRoute(String TGUID, String RGUID) {
        PreparedStatement query;
        String SGUID;
        try {
            query=con.prepareStatement("select Start from Caravans where Finish is null and PGUID=?");
            query.setString(1,GUID);
            ResultSet rs0=query.executeQuery();
            rs0.first();
            SGUID=rs0.getString("Start");
            rs0.close();
            query.close();
            query=con.prepareStatement("select count(1) from Caravans where ((Finish=? and Start=?) or (Finish=? and Start=?)) and PGUID=?");
            query.setString(1,TGUID);
            query.setString(2,SGUID);
            query.setString(3,SGUID);
            query.setString(4,TGUID);
            query.setString(5,GUID);
            ResultSet rs = query.executeQuery();
            rs.first();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return true;
        }
    }

    public String DropUnfinishedRoute() {
        MyUtils.Logwrite("DropUnfinishedRoute","Started by "+Name, r.freeMemory());
        PreparedStatement query;
        try {
            query = con.prepareStatement("delete from Caravans where PGUID=? and Finish is null");
            query.setString(1,GUID);
            query.execute();
            con.commit();
            query.close();
            jresult.put("Result", "OK");
            MyUtils.Logwrite("DropUnfinishedRoute","Finished by "+Name, r.freeMemory());
            return jresult.toString();
        } catch (SQLException e) {jresult.put("Result","DB001");jresult.put("Message","Ошибка обращения к БД"); return jresult.toString();}
    }

    public String DropRoute(String TGUID) {
        MyUtils.Logwrite("DropRoute","Started by "+Name, r.freeMemory());
        /*PreparedStatement query;
        String OwnerGUID;
        try {
            query = con.prepareStatement("select PGUID from Caravans where GUID=?");
            query.setString(1,TGUID);
            ResultSet rs=query.executeQuery();
            rs.first();
            OwnerGUID=rs.getString("PGUID");
            rs.close();
            if (GUID.equals(OwnerGUID)) {
                query = con.prepareStatement("delete from Caravans where PGUID=? and GUID=?");
                query.setString(1, GUID);
                query.setString(2, TGUID);
                query.execute();
                query = con.prepareStatement("delete from GameObjects where GUID=?");
                query.setString(1, TGUID);
                query.execute();
                con.commit();
                query.close();
                jresult.put("Result", "OK");

            }
            else {query.close();jresult.put("Error", "Ты не можешь отменить маршрут другого игрока!");}
        } catch (SQLException e) {jresult.put("Error","DropRoute "+e.toString());}
        */
        jresult.put("Result","DB001");
        jresult.put("Message","Читер что-ли? Отменять маршруты уже нельзя");
        MyUtils.Logwrite("DropRoute",Name+" читер? пытался маршрут отменить.", r.freeMemory());
        MyUtils.Logwrite("DropRoute","Finished by "+Name, r.freeMemory());
        return jresult.toString();
    }

//Ex-Client

    public Connection getCon() {
        return con;
    }

    //TODO move to GenPlayer
    public String temp() {
        PreparedStatement query, query2, query3;
        int p=0;
        int u=0;
        String UGUID, PGUID; // = UUID.randomUUID().toString();
        String result="";
        try {
            query = con.prepareStatement("select GUID from Upgrades WHERE level=0");
            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                u=u+1;
                result=result+"u="+Integer.toString(u)+";";
                UGUID = rs.getString(1);
                query2 = con.prepareStatement("select GUID from Players");
                ResultSet rs2 = query2.executeQuery();
                p=0;
                while (rs2.next()) {
                    p=p+1;
                    result=result+"p="+Integer.toString(p)+";";
                    PGUID = rs2.getString(1);
                    query3 = con.prepareStatement("insert into PUpgrades(PGUID,UGUID) values(?,?) ");
                    query3.setString(1, PGUID);
                    query3.setString(2, UGUID);
                    query3.execute();
                    con.commit();
                    //query3.close();
                }
                rs2.close();
                //query.close();
            }
            rs.close();
//            con.commit();
//            query.close();
            result=result+"Done!";
        } catch (SQLException e) { result = result + e.toString(); }

        try {
            con.close();
        } catch (SQLException e) {result = result + e.toString();}
        return result;
    }

    public String tempExp() {
        PreparedStatement query, query2,query3,query4;
        ResultSet rs,rs2;
        int level;
        long exp, totalexp=0;
        try {
            query = con.prepareStatement("select Exp, Level from Levels where Type='player' order by Level asc");
            rs=query.executeQuery();
            while (rs.next())
            {
                exp=rs.getLong("Exp");
                level=rs.getInt("Level");
                totalexp+=exp*10;
                query2 = con.prepareStatement("update Levels set Exp=? where Type='player' and Level = ?");
                query2.setLong(1,totalexp);
                query2.setInt(2,level);
                query2.execute();
                query2.close();
            }
            rs.close();
            query.close();

            totalexp=0;
            query3 = con.prepareStatement("select Exp, Level from Levels where Type='city' order by Level asc");
            rs2=query3.executeQuery();
            while (rs2.next())
            {
                exp=rs2.getLong("Exp");
                level=rs2.getInt("Level");
                totalexp+=exp*5;
                query4 = con.prepareStatement("update Levels set Exp=? where Type='city' and Level = ?");
                query4.setLong(1,totalexp);
                query4.setInt(2,level);
                query4.execute();
                query4.close();
            }
            rs2.close();
            query3.close();
            con.commit();
        } catch (SQLException e) {return "Error while fixing Exp table " +e.toString();}
        return "Exp table rebuilded!";
    }


    public String sendData(String ReqName, String TGUID, int TLAT, int TLNG, int RACE, int AMOUNT, String text, String ItemType, int Quantity, long clientTime, int GOLD, int OBSIDIAN) {
        //MyUtils.Logwrite("sendData","дошли");
        String result;
        switch (ReqName) {
            case "ScanRange":
                result = ScanRange();
                break;
            case "SetAmbush":
                result = SetAmbush(TLAT, TLNG);
                break;
            case "DestroyAmbush":
                result = DestroyAmbush(TGUID);
                break;
            case "CancelAmbush":
                result = CancelAmbush(TGUID);
                break;
            case "StartRoute":
                result = StartRoute(TGUID);
                break;
            case "FinishRoute":
                result = FinishRoute(TGUID);
                break;
            case "BuyUpgrade":
                result = BuyUpgrade(TGUID);
                break;
            case "DropUnfinishedRoute":
                result = DropUnfinishedRoute();
                break;
            case "DropRoute":
                result = DropRoute(TGUID);
                break;
            case "GetPlayerInfo":
                result = GetPlayerInfo();
                break;
            case "GetRoutes":
                result = GetRoutes();
                break;
            case "GetMessage":
                result=GetMessage();
                break;
            case "SetRace":
                result=setRace(RACE);
                break;
            case "CreateCity":
                result=createCity(TLAT,TLNG);
                break;
            case "HirePeople":
                result=hirePeople(TGUID, AMOUNT);
                break;
            case "FastScan":
                result=fastScan();
                break;
            case "FinishStartRoute":
                result=FinishStartRoute(TGUID);
                break;
            case "OpenChest":
                result=openChest(TGUID);
                break;
            case "SetTower":
                result=setTower(TLAT,TLNG);
                break;
            case "SetTowerText":
                result=setTowerText(TGUID,text);
                break;
            case "GetTowerInfo":
                result=getTowerInfo(TGUID);
                break;
            case "TakeItems":
                result=takeItemsTower(TGUID,ItemType,Quantity);
                break;
            case "PutItems":
                result=putItemsTower(TGUID,ItemType,Quantity);
                break;
            case "Survey":
                result=survey(TLAT,TLNG);
                break;
            case "startExtract":
                result=startExtract(TLAT,TLNG,clientTime);
                break;
            case "finishExtract":
                result=finishExtract(clientTime);
                break;
            case "getPortalInfo":
                result=getPortalInfo();
                Logwrite("Player.getPortalInfo","Finish.");
                break;
            case "portalDonate":
                result=portalDonate(TGUID,GOLD,OBSIDIAN);
                break;
            case "removeSurvey":
                result=removeSurvey(TGUID);
                break;
            case "getResourceInfo":
                result=getResourceInfo();
                break;

            default:
                jresult.put("Result", "DB002");
                jresult.put("Message", "Данный запрос не может быть обработан на сервере!");
                result = jresult.toString();
        }
        try {
            con.close();
        } catch (SQLException e) {jresult.put("Result","DB001");jresult.put("Message","Connection not closed! "+result);result=jresult.toString();}
        return result;
    }

    private String openChest(String TGUID) {
        MyUtils.Logwrite("openChest", "Started by " + Name, r.freeMemory());
        Chest chest = new Chest(con);
        chest.open(GUID, TGUID);
        if (chest.bonus == 0) {
            jresult.put("Result", "O1401");
            jresult.put("Message", "Сундук пуст!");
        } else {
            if (!checkRangeToObj(TGUID)) {
                jresult.put("Result", "O1402");
                jresult.put("Message", "Сундук слишком далеко!");
            } else {
                jresult.put("Result", "OK");
                switch (chest.type) {
                    case "gold":
                        chest.bonus = (int) (chest.bonus * (1 + (float) getPlayerUpgradeEffect2("bargain") / 100));
                        //getGold(chest.bonus);
                        addResource("gold",chest.bonus);
                        addStat("chested", chest.bonus);
                        jresult.put("Gold", chest.bonus);
                        break;
                    case "obsidian":
                        addResource("obsidian",chest.bonus);
                        //commit(con);
                        addStat("obsidianed", chest.bonus);
                        jresult.put("Obsidian", chest.bonus);
                        break;
                    default:
                        jresult.put("Result", "O1401");
                        jresult.put("Message", "Сундук пуст!");
                        break;
                }
                addStat("Nchests", 1);
                chest.delete();
            }
        }
        MyUtils.Logwrite("openChest", "Finished by " + Name, r.freeMemory());
        return jresult.toString();
    }

//Переделать тут всё
    private String setTower(int TLAT, int TLNG) {
        MyUtils.Logwrite("setTower","Started by "+Name, r.freeMemory());
        if (tower) {
            jresult.put("Result","O1501");
            jresult.put("Message", "У Вас уже есть башня!");
        }
        else {
            if (MyUtils.RangeCheck(Lat, Lng, TLAT, TLNG) > getRadius()) {
                jresult.put("Result", "O1502");
                jresult.put("Message", "Слишком далеко!");
            } else {
                if (payResources("Obsidian",5)) {
                    Tower tower = new Tower(con);
                    if (tower.set(GUID, Name, TLAT, TLNG)) {
                        commit(con);
                        jresult.put("Result", "OK");
                        //getObsidian(-5);
                    } else {
                        rollback(con);
                        jresult.put("Result", "DB001");
                        jresult.put("Message", "Ошибка обращения к БД");
                    }
                }
                else {rollback(con);
                    jresult.put("Result", "O1503");
                    jresult.put("Message", "Не хватает обсидиана!");}
            }
        }
        MyUtils.Logwrite("setTower","Finished by "+Name, r.freeMemory());
        return jresult.toString();
    }

    private String setTowerText(String TGUID, String text) {
        MyUtils.Logwrite("setTowerText","Started by "+Name, r.freeMemory());
        if (!tower) {
            jresult.put("Result","O1601");
            jresult.put("Message", "У Вас нет башни!");
        }
        else {
            Tower tower = new Tower(con);
            if (tower.setText(TGUID, text)) {
                jresult.put("Result", "OK");
            } else {
                jresult.put("Result", "DB001");
                jresult.put("Message", "Ошибка обращения к БД");
            }
        }
        MyUtils.Logwrite("setTowerText","Finished by "+Name, r.freeMemory());
        return jresult.toString();
    }

    private String getTowerInfo(String TGUID) {
        MyUtils.Logwrite("getTowerInfo", "Started by " + Name, r.freeMemory());
        Tower tower = new Tower(con);
        String res= tower.getInfo(TGUID);
        MyUtils.Logwrite("getTowerInfo", "Finished by " + Name, r.freeMemory());
        return res;
    }

    private String takeItemsTower(String TGUID, String Type, int Quantity) {
        Tower tower = new Tower(con);
        tower.load(TGUID);
        if (tower.bderror) {
            jresult.put("Result", "DB001");
            jresult.put("Message", "Ошибка обращения к БД.");
        } else if (!tower.loaded) {
            jresult.put("Result", "O1703");
            jresult.put("Message", "Башни не существует!");
        } else if (!checkRangeToObj(TGUID)) {
            jresult.put("Result", "O1702");
            jresult.put("Message", "Башня слишком далеко!");
        } else if (!GUID.equals(tower.PGUID)) {
            jresult.put("Result", "O1701");
            jresult.put("Message", "Это не Ваша башня!");
        } else if (!Type.equals("Obsidian")) {
            jresult.put("Result", "O1704");
            jresult.put("Message", "Работа с данными ресурсами не реализована");
        } else if (Quantity>tower.Obsidian) {
            jresult.put("Result", "O1705");
            jresult.put("Message", "В башне нет столько обсидиана");
        } else {
            jresult.put("Result", "OK");
            jresult.put("Message", "Вы успешно взяли "+Quantity+" обсидиана из башни.");
            tower.Obsidian-=Quantity;Obsidian+=Quantity;update();tower.update();
        }
        return jresult.toString();
    }

    private String putItemsTower(String TGUID, String Type, int Quantity) {
        Tower tower = new Tower(con);
        tower.load(TGUID);
        if (tower.bderror) {
            jresult.put("Result", "DB001");
            jresult.put("Message", "Ошибка обращения к БД.");
        } else if (!tower.loaded) {
            jresult.put("Result", "O2003");
            jresult.put("Message", "Башни не существует!");
        } else if (!checkRangeToObj(TGUID)) {
            jresult.put("Result", "O2002");
            jresult.put("Message", "Башня слишком далеко!");
        } else if (Race!=tower.Race) {
            jresult.put("Result", "O2001");
            jresult.put("Message", "Это башня другой фракции! Ай-ай-ай!");
        } else if (!Type.equals("Obsidian")) {
            jresult.put("Result", "O2004");
            jresult.put("Message", "Работа с данными ресурсами не реализована");
        } else if (Quantity>Obsidian) {
            jresult.put("Result", "O2005");
            jresult.put("Message", "У Вас нет столько обсидиана");
        } else {
            jresult.put("Result", "OK");
            jresult.put("Message", "Вы успешно положили "+Quantity+" обсидиана в башню.");
            tower.Obsidian+=Quantity;Obsidian-=Quantity;update();tower.update();
        }
        return jresult.toString();
    }

    private String hirePeople(String TGUID, int AMOUNT) {
        MyUtils.Logwrite("hirePeople","Started by "+Name, r.freeMemory());
        int hireCost; float RaceBonus,RaceDiscount;
        City city = new City(TGUID,con);
        if (!checkRangeToObj(TGUID)) {
            jresult.put("Result", "O1302");
            jresult.put("Message", "Город слишком далеко!");
            MyUtils.Logwrite("hirePeople",Name+" Город слишком далеко!", r.freeMemory());}
        else {
            if (city.Hirelings < AMOUNT) {
                jresult.put("Result", "O1305");
                jresult.put("Message", "В городе нет столько наемников!");
                MyUtils.Logwrite("hirePeople",Name+" В городе нет столько наемников!", r.freeMemory());
            } else {
                if (Hirelings + HirelingsInAmbushes + AMOUNT > 95+Level*(100+5*Level)) {
                    jresult.put("Result", "O1304");
                    jresult.put("Message", "Вы пока не можете управлять таким количеством наемников!");
                    MyUtils.Logwrite("hirePeople",Name+" Вы пока не можете управлять таким количеством наемников!", r.freeMemory());
                } else {
                    RaceBonus=0;
                    int totalInfluence=city.Influence1+city.Influence2+city.Influence3;
                    float conc = 0;
                    if (totalInfluence>0) {
                        int maxInfluence = Math.max(Math.max(city.Influence1,city.Influence2),city.Influence3);
                        conc= (float)(3*maxInfluence - totalInfluence)/(4*totalInfluence);
                        if (Race==1) {RaceBonus=(float)city.Influence1/totalInfluence;}
                        if (Race==2) {RaceBonus=(float)city.Influence2/totalInfluence;}
                        if (Race==3) {RaceBonus=(float)city.Influence3/totalInfluence;}
                    }
                    RaceDiscount=1-RaceBonus/4;
                    hireCost = (int) ((1+conc)*(RaceDiscount * AMOUNT * (int) (100 * Math.sqrt(city.Level)) * (100 - getPlayerUpgradeEffect1("bargain")) / 100));

                    if (!payResources("Gold",hireCost)) {
                        rollback(con);
                        jresult.put("Result", "O1303");
                        jresult.put("Message", "Вам не хватает денег! Требуется " + hireCost + " золота!");
                        MyUtils.Logwrite("hirePeople",Name+" Вам не хватает денег! Требуется " + hireCost + " золота!", r.freeMemory());
                    } else {
                        Gold -= hireCost;
                        city.Hirelings -= AMOUNT;
                        city.getGold((int)(hireCost/((1+conc)*(1+conc))));
                        Hirelings += AMOUNT;
                        addResource("Hirelings",AMOUNT);
                        city.update();
                        update();
                        jresult.put("Result", "OK");
                        jresult.put("Message", "Вы успешно наняли " + AMOUNT + " наемников");
                        MyUtils.Logwrite("hirePeople",Name+" Вы успешно наняли " + AMOUNT + " наемников", r.freeMemory());
                    }
                }
            }
        }
        MyUtils.Logwrite("hirePeople","Finished by "+Name, r.freeMemory());
        return jresult.toString();
    }

    public String getRandomCity() {
        String CGUID;
        int total,rand;
        PreparedStatement query;
        ResultSet rs;
        try {
            query = con.prepareStatement("select GUID from Cities where Creator=?");
            query.setString(1,GUID);
            rs=query.executeQuery();
            rs.last();
            total=rs.getRow();
            if (total>0) {
                MyUtils.Logwrite("getRandomCity", "total=" + total);
                Random random = new Random();
                rand = 1 + random.nextInt(total);
                MyUtils.Logwrite("getRandomCity", "rand=" + rand);
                rs.absolute(rand);
                MyUtils.Logwrite("getRandomCity", "отработал rs.absolute");
                CGUID = rs.getString("GUID");
                MyUtils.Logwrite("getRandomCity", "CGUID=" + CGUID);
            }
            else CGUID="";
        } catch (SQLException e) {MyUtils.Logwrite("Player.getRandomCity",e.toString());CGUID="";}
        return CGUID;
    }

    private int foundedCities() {
        int founded;
        try {
            PreparedStatement query = con.prepareStatement("select count(1) from Cities where Creator=? and kvant=0");
            query.setString(1, GUID);
            ResultSet rs = query.executeQuery();
            rs.first();
            founded = rs.getInt(1);
            rs.close();
            query.close();
        } catch (SQLException e) {
            founded = 999999;
        }
        return founded;
    }

    private String createCity(int TLAT, int TLNG) {
        String res, CGUID;
        //int mapper;
        MyUtils.Logwrite("createCity","Started by "+Name, r.freeMemory());
        if (MyUtils.RangeCheck(Lat, Lng, TLAT, TLNG) <= getRadius()) {
            int citiesFounded=foundedCities();
            int goldToNextCity=Math.max(0,(citiesFounded-10)* 10000);
            int obsidianToNextCity=Math.max(0,(citiesFounded-10));
            if (payResources("Gold",goldToNextCity) && payResources("Obsidian",obsidianToNextCity)) {
                CGUID=UUID.randomUUID().toString();
                City city = new City(con,CGUID);
                //mapper=getPlayerUpgradeEffect2("founder");
                jresult = city.createCity(GUID, TLAT, TLNG);
                int actionExp=2500+25*getEffect("exp"); //2500*(1+getEffect("exp")/100);
                getExp(actionExp);
                commit(con);
                jresult.put("Exp",actionExp);
                jresult.put("Level",Level);
                jresult.put("isLevelChanged",flagLevelChanged);
            } else {
                jresult.put("Result","O1203");
                jresult.put("Message", "Не хватает ресурсов для основания нового города!");
                //res=jresult.toString();
            }

        } else {
            jresult.put("Result","O1201");
            jresult.put("Message", "Слишком далеко!");
            //res=jresult.toString();
        }
        MyUtils.Logwrite("createCity","Finished by "+Name, r.freeMemory());
        return jresult.toString();
    }

    private String GetMessage() {
        MyUtils.Logwrite("GetMessage","Started by "+Name, r.freeMemory());
        PreparedStatement query, query2;
        ResultSet rs;
        try {
            query = con.prepareStatement("select GUID,PGUID,Message,Type,State,Lat,Lng,Time from Messages where State<100 and PGUID=? order by Time");
            query.setString(1,GUID);
            rs=query.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    JSONObject jobj = new JSONObject();
                    jobj.put("GUID",rs.getString("GUID"));
                    //jobj.put("PGUID",rs.getString("PGUID"));
                    jobj.put("Message",rs.getString("Message"));
                    jobj.put("Type",rs.getInt("Type"));
                    jobj.put("State",rs.getInt("State"));
                    jobj.put("TargetLat",rs.getInt("Lat"));
                    jobj.put("TargetLng",rs.getInt("Lng"));
                    jobj.put("Time",rs.getTimestamp("Time").getTime());
                    jarr.add(jobj);
                    query2 = con.prepareStatement("update Messages set State=100 where GUID=?");
                    query2.setString(1,rs.getString("GUID"));
                    query2.execute();
                    query2.close();
                    con.commit();
                }
            }
            jresult.put("Messages",jarr);
        } catch (SQLException e) {jresult.put("Result","DB001");jresult.put("Message","Ошибка обращения к БД");}
        MyUtils.Logwrite("GetMessage","Finished by "+Name, r.freeMemory());
        return jresult.toString();
    }

    public String GetToken(String Login, String Password) {
        PreparedStatement pstmt,query;
        String Token = "T" + UUID.randomUUID().toString();
        String PGUID;
        //      JSONObject jresult = new JSONObject();
        try {
            pstmt = con.prepareStatement("SELECT GUID from Users WHERE Login=? and Password=?");
            pstmt.setString(1, Login);
            pstmt.setString(2, Password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                PGUID = rs.getString(1);
                query=con.prepareStatement("select count(1) from Players where GUID=?");
                query.setString(1,PGUID);
                ResultSet rs2=query.executeQuery();
                rs2.first();
                if (rs2.getInt(1)==0) {register(Login, Password);}
                rs2.close();
                query.close();
                pstmt = con.prepareStatement("INSERT into Connections (PGUID,Token) Values(?,?)");
                pstmt.setString(1, PGUID);
                pstmt.setString(2, Token);
                pstmt.execute();
                con.commit();
                con.close();
                jresult.put("Token", Token);
            } else {
                jresult.put("Error", "User not found.");
                con.close();
            }
        } catch (SQLException e) {
            jresult.put("Error", "DBError. " + e.toString() + ". " + Arrays.toString(e.getStackTrace()));
        } /*catch (NamingException e) {
            jresult.put("Error", "ResourceError. " + e.toString() + ". " + Arrays.toString(e.getStackTrace()));
        }*/
        return jresult.toString();
    }
//End of Ex-Client

/*    private void countSurvey(int TLAT, int TLNG, String restype)
    {
        int scaleLat, scaleLng;
        int quantity=1000;
        double extractKoef=0.1;
        //будем оперировать участками по 100 lat-lng, работаем с округленными значениями:
        int tlat=100*(TLAT/100);
        int tlng=100*(TLNG/100);
        switch (restype) {
            case "wood":
                scaleLat=1000;
                scaleLng=2000;
                maxQuantity=(int)(quantity*Math.min(Math.max(0,Math.cos((double)tlat/scaleLat)),Math.max(0,Math.cos((double)tlng/scaleLng))));
                break;
            case "grain":
                scaleLat=2000;
                scaleLng=2000;
                maxQuantity=(int)(quantity*Math.min(Math.max(0,Math.sin((double)tlat/scaleLat)),Math.max(0,Math.cos((double)tlng/scaleLng))));
                break;
            case "stone":
                scaleLat=2000;
                scaleLng=1000;
                maxQuantity=(int)(quantity*Math.min(Math.max(0,Math.cos((double)tlat/scaleLat)),Math.max(0,Math.sin((double)tlng/scaleLng))));
                break;
        }

        //вычитываваем раскопки в радиусе из базы, уменьшаем доход (возвращаем два значения, максимум и текущий

        int deltaLat=4491; //2km
        int deltaLng=(int)(deltaLat/Math.cos((TLAT / 1e6) * Math.PI / 180));

        try {
            PreparedStatement query = con.prepareStatement("select count(1) from extraction where ? between lat - ? and lat + ? and ? between lng - ? and lng + ? and type=?");
            query.setInt(1,tlat);
            query.setInt(2,deltaLat);
            query.setInt(3,deltaLat);
            query.setInt(4, tlng);
            query.setInt(5,deltaLng);
            query.setInt(6,deltaLng);
            query.setString(7,restype);
            ResultSet rs = query.executeQuery();
            rs.first();
            int extracts=rs.getInt(1);
            if (isBetween(extracts,0,4)) {extractKoef=1.0;}
            else if (isBetween(extracts,5,9)) {extractKoef=0.9;}
            else if (isBetween(extracts,10,14)) {extractKoef=0.8;}
            else if (isBetween(extracts,15,19)) {extractKoef=0.7;}
            else if (isBetween(extracts,20,24)) {extractKoef=0.6;}
            else if (isBetween(extracts,25,29)) {extractKoef=0.5;}
            else if (isBetween(extracts,30,34)) {extractKoef=0.4;}
            else if (isBetween(extracts,35,39)) {extractKoef=0.3;}
            else if (isBetween(extracts,40,49)) {extractKoef=0.2;}
            else {extractKoef=0.1;}
        }
        catch (SQLException e) {Logwrite("countSurvey","SQL error: "+e.toString());maxQuantity=-1;}

        currentQuantity=(int)(maxQuantity*extractKoef);
    }
*/
    private boolean updateExtraction(int TLAT, int TLNG, String restype) {
        try {
            PreparedStatement query=con.prepareStatement("insert into extraction values(?,?,?,NOW())");
            query.setInt(1,TLAT);
            query.setInt(2,TLNG);
            query.setString(3,restype);
            query.execute();
            //con.commit();
            return true;
        }
        catch (SQLException e) {Logwrite("updateExtraction","SQL error: "+e.toString());return false;}
    }

    private boolean updateSurveys(int TLAT, int TLNG, String restype) {
        String SGUID=UUID.randomUUID().toString();
        try {
            PreparedStatement query=con.prepareStatement("insert into surveys values(?,?,?,?,?,?,?,NOW())");
            query.setString(1, SGUID);
            query.setString(2, GUID);
            query.setInt(3,TLAT);
            query.setInt(4,TLNG);
            query.setString(5,restype);
            query.setInt(6,maxQuantity);
            query.setInt(7,currentQuantity);
            query.execute();
            query=con.prepareStatement("insert into GameObjects values(?,?,?,'survey')");
            query.setString(1, SGUID);
            query.setInt(2,TLAT);
            query.setInt(3,TLNG);
            query.execute();
            return true;
        }
        catch (SQLException e) {Logwrite("updateSurveys","SQL error: "+e.toString());return false;}
    }

    private String survey(int TLAT, int TLNG) {

        try {
            PreparedStatement query=con.prepareStatement("insert into surveys (GUID,PGUID,lat,lng,created) values (?,?,?,?,NOW())");
            query.setString(1,UUID.randomUUID().toString());
            query.setString(2,GUID);
            query.setInt(3,TLAT);
            query.setInt(4,TLNG);
            query.execute();
            con.commit();
            jresult.put("Result","OK");
        }
        catch (SQLException e) {
            Logwrite("survey","SQL error: "+e.toString());
            jresult.put("Result", "DB001");
        }

        return jresult.toString();
    }

    private String removeSurvey(String TGUID) {
        try {
            PreparedStatement query=con.prepareStatement("delete from surveys where GUID=?");
            query.setString(1,TGUID);
            query.execute();
            con.commit();
            jresult.put("Result","OK");
            }
        catch (SQLException e) {
            Logwrite("removeSurvey","SQL error: "+e.toString());jresult.put("Result", "DB001");
            }
        return jresult.toString();
    }

    private void cancelUnfinishedExtraction() {
        try {
            PreparedStatement query=con.prepareStatement("delete from extraction where PGUID=? and finished is null");
            query.setString(1,GUID);
            query.execute();
            con.commit();
        }
        catch (SQLException e) {
            Logwrite("cancelUnfinishedExtraction","SQL Error: "+e.toString());
        }
    }

    private boolean addEntryToExtraction(int TLAT, int TLNG, long startTime) {
        Logwrite("addEntry","Start");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date data = new Date(startTime);
        String sTime = dateFormat.format(data);
        Logwrite("addEntry","startTime="+sTime);
        try {
            PreparedStatement query=con.prepareStatement("insert into extraction (PGUID,lat,lng,started,clientStarted) values (?,?,?,NOW(),?)");
            query.setString(1,GUID);
            query.setInt(2,TLAT);
            query.setInt(3,TLNG);
            query.setString(4,sTime);
            query.execute();
            con.commit();
            return true;
        }
        catch (SQLException e) {
            Logwrite("addEntryToExtraction","SQL Error: "+e.toString());
            return false;
        }
    }

    //TODO рефакторинг нужен, скопировал ее в World
    private double koefDepletion(int TLAT, int TLNG) {
        double extractKoef=0.0;
        try {
            PreparedStatement query=con.prepareStatement("select count(1) from extraction where floor(lat / ?) = ? and floor(lng / ?) = ? and finished>NOW() - INTERVAL 1 DAY");
            query.setInt(1,Params.resLatSize);
            query.setInt(2,TLAT/Params.resLatSize);
            query.setInt(3,Params.resLngSize);
            query.setInt(4,TLNG/Params.resLngSize);
            ResultSet rs=query.executeQuery();
            rs.first();
            int extracts=rs.getInt(1);
            if (isBetween(extracts,0,19)) {extractKoef=1.0;}
            else if (isBetween(extracts,20,39)) {extractKoef=0.9;}
            else if (isBetween(extracts,40,59)) {extractKoef=0.8;}
            else if (isBetween(extracts,60,79)) {extractKoef=0.7;}
            else if (isBetween(extracts,80,99)) {extractKoef=0.6;}
            else if (isBetween(extracts,100,119)) {extractKoef=0.5;}
            else if (isBetween(extracts,120,139)) {extractKoef=0.4;}
            else if (isBetween(extracts,140,159)) {extractKoef=0.3;}
            else if (isBetween(extracts,160,179)) {extractKoef=0.2;}
            else {extractKoef=0.1;}
        }
        catch (SQLException e) {
            Logwrite("Player.koefDepletion","SQL Error: "+e.toString());
        }
        return extractKoef;
    }

    private JSONObject getResourcesFromExtraction(int TLAT, int TLNG) {
        Random random;
        JSONObject jobj = new JSONObject();
        JSONObject jres = new JSONObject();
        //в сюрвее должна быть инфа, тянем оттуда
        try {
            PreparedStatement query=con.prepareStatement("select type, maxQuantity,maxQuantity2,maxQuantity3 from surveys where PGUID=? and floor(lat / ?) = ? and floor(lng / ?) = ? and done=1");
            query.setString(1,GUID);
            query.setInt(2,Params.resLatSize);
            query.setInt(3,TLAT/Params.resLatSize);
            query.setInt(4,Params.resLngSize);
            query.setInt(5,TLNG/Params.resLngSize);
            ResultSet rs=query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                String type1=rs.getString("type");
                String type2="";
                String type3="";
                if (type1.equals("stone")) {
                    type2="obsidian";
                    type3="iron";
                }
                if (type1.equals("wood")) {
                    type2="amber";
                    type3="redwood";
                }
                if (type1.equals("grain")) {
                    type2="hop";
                    type3="wool";
                }
                double koefDepl=koefDepletion(TLAT, TLNG);

                int quantity=0;
                jobj.put("Type",type1);
                int maxProb=rs.getInt("maxQuantity");
                //jobj.put("MaxProb",maxProb);
                int prob=(int) (maxProb*koefDepl);
                //jobj.put("Prob",prob);
                random = new Random();
                if (prob>=random.nextInt(100)) {
                    quantity=1;
                    addResource(type1,quantity);
                }
                jobj.put("Quantity", quantity);
                jarr.add(jobj);
                quantity=0;

                jobj=new JSONObject();
                jobj.put("Type",type2);
                maxProb=rs.getInt("maxQuantity2");
                //jobj.put("MaxProb",maxProb);
                prob=(int) (maxProb*koefDepl);
                //jobj.put("Prob",prob);
                random = new Random();
                if (prob>=random.nextInt(100)) {
                    quantity=1;
                    addResource(type2,quantity);
                }
                jobj.put("Quantity", quantity);
                quantity=0;

                jarr.add(jobj);
                jobj=new JSONObject();
                jobj.put("Type",type3);
                maxProb=rs.getInt("maxQuantity3");
                //jobj.put("MaxProb",maxProb);
                prob=(int) (maxProb*koefDepl);
                //jobj.put("Prob",prob);
                random = new Random();
                if (prob>=random.nextInt(100)) {
                    quantity=1;
                    addResource(type3,quantity);
                }
                jobj.put("Quantity", quantity);
                jarr.add(jobj);
                jres.put("Result","OK");
                jres.put("Survey", jarr);
            }
            else {
                //вообще сюда не должны попадать
                Logwrite("getResourcesFromExtraction",GUID + " не получил ресурсы, т.к. не найден завершенный survey для области TLAT="+TLAT+", TLNG="+TLNG);
                jres.put("Result","O2401");
            }
        }
        catch (SQLException e) {
            Logwrite("getResourcesFromExtraction","SQL Error: "+e.toString());
            jres.put("Result","DB001");
        }
    return jres;
    }

    private String startExtract(int TLAT, int TLNG, long startTime) {
        //проверить на сюрвей?
        try {
            PreparedStatement query = con.prepareStatement("select 1 from surveys where PGUID=? and floor( lat / ?) = ? and floor( lng / ?) = ? and done=1");
            query.setString(1, GUID);
            query.setInt(2, Params.resLatSize);
            query.setInt(3, TLAT / Params.resLatSize);
            query.setInt(4, Params.resLngSize);
            query.setInt(5, TLNG / Params.resLngSize);
            ResultSet rs = query.executeQuery();
            if (!rs.isBeforeFirst()) {
                jresult.put("Result","O2101");
                Logwrite("startExtract","GUID="+GUID+", TLAT="+TLAT+", TLNG="+TLNG+", Params.resLatSize="+Params.resLatSize+", TLAT / Params.resLatSize="+Integer.toString(TLAT / Params.resLatSize)+", Params.resLngSize="+Params.resLngSize+", TLNG / Params.resLngSize="+Integer.toString(TLAT / Params.resLngSize));
                return jresult.toString();
            }
        }
        catch (SQLException e) {
            jresult.put("Result","DB001");
            Logwrite("startExtract","SQL Error: "+e.toString());
            return jresult.toString();
        }

        cancelUnfinishedExtraction();
        if (addEntryToExtraction(TLAT, TLNG, startTime)) {
            jresult.put("Result","OK");
        }
        else {
            jresult.put("Result","DB001");
        }
        return jresult.toString();
    }

    private boolean finishEntryInExtraction(long finishTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date data=new Date(finishTime);
        String fTime = dateFormat.format(data);
        Logwrite("finishEntry","finishTime="+fTime);

        try {
            PreparedStatement query=con.prepareStatement("update extraction set finished=NOW(), clientFinished=? where PGUID=? and finished is null");
            query.setString(1,fTime);
            query.setString(2,GUID);
            query.execute();
            con.commit();
            return true;
        }
        catch (SQLException e) {
            Logwrite("finishEntryInExtraction","SQL Error: "+e.toString());
            return false;
        }
    }

    private String finishExtract(long finishTime) {
        Logwrite("finishExtract",Name+" started");
        int TLAT,TLNG;
        try {
            PreparedStatement query=con.prepareStatement("select lat,lng from extraction where finished is null and PGUID=?");
            query.setString(1,GUID);
            ResultSet rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                TLAT=rs.getInt("lat");
                TLNG=rs.getInt("lng");
                if (finishEntryInExtraction(finishTime)) {
                    jresult=getResourcesFromExtraction(TLAT,TLNG);
                    con.commit();
                    Logwrite("finishExtract",Name+" finished");
                }
                else jresult.put("Result","DB001");
            }
            else {
                jresult.put("Result","O2402"); //Не найдена начатая добыча ресурса
            }
        }
        catch (SQLException e) {
            Logwrite("finishExtract","SQL Error: "+e.toString());
            jresult.put("Result","DB001");
        }
        return jresult.toString();
    }


    public boolean addResource(String type, int quantity) {
        try {
            PreparedStatement query=con.prepareStatement("update resources set quantity=quantity+? where PGUID=? and type=?");
            query.setInt(1,quantity);
            query.setString(2, GUID);
            query.setString(3,type);
            query.execute();
            //con.commit();
            return true;
        }
        catch (SQLException e) {Logwrite("addResources","SQL error: "+e.toString());return false;}
    }

    public boolean writeResource(String type, int quantity) {
        try {
            PreparedStatement query=con.prepareStatement("update resources set quantity=? where PGUID=? and type=?");
            query.setInt(1,quantity);
            query.setString(2, GUID);
            query.setString(3,type);
            query.execute();
            //con.commit();
            return true;
        }
        catch (SQLException e) {Logwrite("writeResources","SQL error: "+e.toString());return false;}
    }

    public int readResource(String type) {
        try {
            PreparedStatement query=con.prepareStatement("select quantity from resources where PGUID=? and type=?");
            query.setString(1, GUID);
            query.setString(2,type);
            ResultSet rs=query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                return rs.getInt(1);
            }
        }
        catch (SQLException e) {Logwrite("readResources","SQL error: "+e.toString());}
        return 0;
    }

    public boolean payResources(String type, int quantity) {
        int curRes = readResource(type);
        if (curRes>=quantity) {writeResource(type,curRes-quantity); return true;}
        else return false;
    }

    public String castSpell(String name) {
        try {
            if (hasSpell(name) && canCast(name) && setEffects(name)) {
                con.commit();
                jresult.put("Result", "OK");
            } else {
                con.rollback();
                jresult.put("Result", result);
            }
        }
        catch (SQLException e) {jresult.put("Result","DB0001");Logwrite("Player.castSpell","SQL Error: "+e.toString());}
        return jresult.toString();
    }

    private boolean hasSpell(String name) {
        try {
            PreparedStatement query = con.prepareStatement("select 1 from Pinv where type='spell' and id=?");
            query.setString(1, name);
            ResultSet rs = query.executeQuery();
            if (rs.isBeforeFirst()) return true;
            else return false;
        }
        catch (SQLException e) {result="DB001";Logwrite("Player.hasSpell","SQL Error: "+e.toString());return false;}
    }

    private boolean canCast(String name) {
        try {
            PreparedStatement query = con.prepareStatement("select reqtype, value from requirements where type='spell' and id=?");
            query.setString(1, name);
            ResultSet rs = query.executeQuery();
            if (!rs.isBeforeFirst()) {return true;}
            else {
                while (rs.next()) {
                    //тут надо сравнить требования с тем, что у игрока есть}
                }
                return false;
            }
        }
        catch (SQLException e) {result="DB001";Logwrite("Player.canCast","SQL Error: "+e.toString());return false;}
    }

    private boolean setEffects(String name) {
        boolean successFlag=false;
        switch (name) {
            case "concentration":
            case "speed":
            case "defence":
            case "pillage":
            case "eagleeye":
            case "longhands":
                if (checkBuffsOn(name)) {successFlag=updateBuff(name);}
                else {successFlag=insertBuff(name);}


        }
        /* тут надо проверить, что такие эффекты еще не висят. если висят, то апдейтнуть, если нет, то инсертнуть
        try {

            PreparedStatement query = con.prepareStatement("select effect, value, time from effects where type='spell' and id=?");
            query.setString(1, name);
            ResultSet rs = query.executeQuery();
            if (rs.isBeforeFirst()) return true;
            else return false;

        }
        catch (SQLException e) {result="DB001";Logwrite("Player.hasSpell","SQL Error: "+e.toString());return false;}
        */
        return false;
    }

    private boolean checkBuffsOn(String name)
    {
        try{
            PreparedStatement query=con.prepareStatement("select 1 from effectsOn where PGUID=? and id=? and type='spell'");
            query.setString(1,GUID);
            query.setString(2,name);
            ResultSet rs=query.executeQuery();
            if (rs.isBeforeFirst()) {return true;}
            else {return false;}
        }
        catch (SQLException e) {Logwrite("Player.checkBuffsOn","SQL Error: "+e.toString());return false;}
    }

    private boolean updateBuff(String name) {
        try {
            PreparedStatement query = con.prepareStatement("update effectsOn z1, effects z2 set z1.finishTime=NOW()+z2.time where z1.id= ? and z2.id=? and z2.type=z1.type");
            query.setString(1,name);
            query.execute();
        }
        catch (SQLException e) {Logwrite("Player.updateBuff","SQL Error: "+e.toString());return false;}
        return false;
    }

    private boolean insertBuff(String name) {
        try {
            PreparedStatement query = con.prepareStatement("insert into effectsOn (PGUID, id, type, finishTime) VALUES (?,?,'spell', (select NOW()+time/1440 from effects where id=? and type='spell'))");
            query.setString(1,GUID);
            query.setString(2,name);
            query.setString(3,name);
            query.execute();
        }
        catch (SQLException e) {Logwrite("Player.insertBuff","SQL Error: "+e.toString());return false;}
        return false;

    }

    private String getPortalInfo() {
        Logwrite("Player.getPortalInfo","Start.");
        Portal portal = new Portal(Race, con);
        Logwrite("Player.getPortalInfo","Загрузили данные портала.");
        return portal.getInfo().toString();
    }

    private String portalDonate(String TGUID, int GOLD, int OBSIDIAN) {
        Logwrite("Player.portalDonate", "Start. Gold=" + GOLD + ". Obsidian=" + OBSIDIAN);
        if (!checkRangeToObj(TGUID)) {
            jresult.put("Result", "O2202");
            jresult.put("Message", "Башня слишком далеко!");
        } else {
            if (payResources("Gold", GOLD) && payResources("Obsidian", OBSIDIAN)) {
                //commit(con);
                JSONObject jobj = new JSONObject();
                Portal portal = new Portal(Race, con);
                jresult = portal.Donate(GOLD, OBSIDIAN);

                if (jresult.toString().contains("DB001")) {
                    rollback(con);
                    return jresult.toString();
                }

                addStat("donatedGold", GOLD);
                addStat("donatedObsidian", OBSIDIAN);

                jobj.put("Type", "Gold");
                jobj.put("Quantity", readResource("Gold"));

                jobj = new JSONObject();
                jobj.put("Type", "Obsidian");
                jobj.put("Quantity", readResource("Obsidian"));
                jarr.add(jobj);
                jresult.put("playerRes", jarr);
                jresult.put("Result", "OK");
                Logwrite("Player.portalDonate", "Finish. OK");
            } else {
                jresult.put("Result", "O2201");
                Logwrite("Player.portalDonate", "Finish. Error (Не хватает ресурсов для доната).");
            }
        }
        return jresult.toString();
    }

    protected int getEffect(String effectType) {
        int res;
        PreparedStatement query;
        try {
            query = con.prepareStatement("select sum(value) from effects where (PGUID=? or PGUID=?) and effect=? and (time is null or time>=NOW())");
            query.setString(1,GUID);
            query.setString(2,Integer.toString(Race));
            query.setString(3,effectType);
            ResultSet rs=query.executeQuery();
            rs.first();
            res=rs.getInt(1);
            rs.close();
            query.close();
        } catch (SQLException e) {Logwrite("getEffect","SQL Error: "+e.toString());return 0;}
        return res;
    }

    private String getResourceInfo() {
        JSONObject jobj;
        JSONArray jarr;
        PreparedStatement query;
        try {
            query = con.prepareStatement("select type,quantity from resources where PGUID=?");
            query.setString(1,GUID);
            ResultSet rs=query.executeQuery();
            jarr = new JSONArray();
            while (rs.next()) {
                jobj = new JSONObject();
                jobj.put("Type",rs.getString("type"));
                jobj.put("Quantity",rs.getInt("quantity"));
                jarr.add(jobj);
            }
            jresult.put("Result", "OK");
            jresult.put("Resources",jarr);
            rs.close();
            query.close();
        } catch (SQLException e) {
            Logwrite("getResInfo","SQL Error: "+e.toString());
            jresult.put("Result","DB001");
        }
        return jresult.toString();
    }

}

