package main;

import org.json.simple.JSONObject;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;


/**
 * Created by Well on 30.01.2016.
 */
public class World {
    Connection con;
    String result;

public World() throws SQLException {
    try {
        con = DBUtils.ConnectDB();
    } catch (SQLException | NamingException e) {
        e.printStackTrace();
    }
}

    public void moveFast() {
        tickAmbushes();
        moveAllCaravans();
        handleAmbushes();
        handleFinishedCaravans();
    }

    public void moveHour() {
        citiesHire();
    }

    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("World.close",e.toString());
        }
    }

    private void citiesHire() {
        PreparedStatement query;
        MyUtils.Logwrite("World.citiesHire", "Start");
        try {
            query = con.prepareStatement("update Cities set Hirelings=LEAST(100*Level,Hirelings+4*Level) where Hirelings<100*Level");
            query.execute();
            con.commit();
            query.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("World.citiesHire", "SQL Error: " + e.toString());
        }
        MyUtils.Logwrite("World.citiesHire", "Finish");
    }

    private void tickAmbushes() {
        PreparedStatement query;
        MyUtils.Logwrite("World.tickAmbushes", "Start");
        try {
            query = con.prepareStatement("update Ambushes set TTS=TTS+1");
            query.execute();
            query.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("World.tickAmbushes", "SQL Error: " + e.toString());
        }
        MyUtils.Logwrite("World.tickAmbushes", "Finish");
    }

    private void moveAllCaravans() {
        PreparedStatement query;
        MyUtils.Logwrite("World.moveAllCaravans", "Start");
        try {
            //пересчитали скорость
            query = con.prepareStatement("update Caravans z1, PUpgrades z3, Upgrades z4 set z1.Lifetime=z1.Lifetime+1, z1.speed=sign(z1.speed)*least(abs(z1.speed)+z4.effect1,z4.effect2) where z1.PGUID=z3.PGUID and z3.UGUID=z4.GUID and z4.Type='speed'");
            query.execute();
            query.close();
            //изменили координаты караванов
            query = con.prepareStatement("update Caravans z1, GameObjects z2, GameObjects z3, GameObjects z4 set z2.Lat=round(z2.Lat+(z4.Lat-z3.Lat)*z1.Speed/z1.Distance), z2.Lng=round(z2.Lng+(z4.Lng-z3.Lng)*z1.Speed/z1.Distance) where z1.GUID=z2.GUID and z1.Start=z3.GUID and z1.Finish=z4.GUID");
            query.execute();
            query.close();
            con.commit();
        } catch (SQLException e) {
            MyUtils.Logwrite("World.moveAllCaravans", "SQL Error: " + e.toString());
        }
        MyUtils.Logwrite("World.moveAllCaravans", "Finish");
    }
/*
    private void fastAmbushes() {
        String CGUID, CPGUID, AGUID, APGUID, AName, SName, FName, prevCaravan="111",Start,Finish;
        int ALife, Bonus, SLevel, FLevel, Distance, ALat, ALng, PLat, PLng, Lat,Lng, Speed;
        PreparedStatement query,query2;
        Player player;
        ResultSet rs;
        int deltaLat = 1125;
        int deltaLng = 2500;
        MyUtils.Logwrite("World.fastAmbushes", "Start");
        try {
            //query = con.prepareStatement("select z1.GUID as AGUID, z3.GUID as CGUID, z2.Life, z2.Name, z2.PGUID, z2.Radius, z2.TTS from GameObjects z1, Ambushes z2, GameObjects z3 where z2.GUID=z1.GUID and z3.Lat between z1.Lat-1125 and z1.Lat+1125 and z3.Lng between z1.Lng-2500 and z1.Lng+2500 and z1.Type='Ambush' and z3.Type='Caravan' and z2.Radius>=round(6378137 * acos(cos(z1.Lat / 1e6 * PI() / 180) * cos(z3.Lat / 1e6 * PI() / 180) * cos(z1.Lng / 1e6 * PI() / 180 - z3.Lng / 1e6 * PI() / 180) + sin(z1.Lat / 1e6 * PI() / 180) * sin(z3.Lat / 1e6 * PI() / 180)))");
            query = con.prepareStatement("select z1.GUID as AGUID, z1.Lat as ALat, z1.Lng as ALng, z3.GUID as CGUID, z3.Lat as CLat, z3.Lng as CLng from GameObjects z1, GameObjects z3 where z3.Lat between z1.Lat-1125 and z1.Lat+1125 and z3.Lng between z1.Lng-2500 and z1.Lng+2500 and z1.Type='Ambush' and z3.Type='Caravan'");
            rs=query.executeQuery();
            while (rs.next()) {
                AGUID=rs.getString("AGUID");
                CGUID=rs.getString("CGUID");
            }
        } catch (SQLException e) {
            MyUtils.Logwrite("World.handleAmbushes", "SQL Error: " + e.toString());
        }
    }
*/
    private void handleAmbushes() {
        String CGUID, CPGUID, AGUID, APGUID, AName, SName, FName, prevCaravan="111",Start,Finish;
        int ALife, Bonus, SLevel, FLevel, Distance, ALat, ALng, PLat, PLng, Lat,Lng, Speed;
        PreparedStatement query,query2;
        Player player;
        ResultSet rs;
        int deltaLat = 1125;
        int deltaLng = 2500; //Надо бы как-то считать наверное
        MyUtils.Logwrite("World.handleAmbushes", "Start");
        try {
            query = con.prepareStatement("select z4.Start,z4.Finish,z4.Speed,z4.GUID as CGUID, z4.PGUID as CPGUID, z3.Lat as PLat, z3.Lng as PLng, z2.GUID as AGUID, z2.PGUID as APGUID, z2.Life as ALife, z2.Name as AName, z1.Lat as ALat, z1.Lng as ALng, (select level from Cities where GUID=z4.Start) as SLevel, (select Name from Cities where GUID=z4.Start) as SName, (select level from Cities where GUID=z4.Finish) as FLevel, (select Name from Cities where GUID=z4.Finish) as FName, z4.Distance, z4.Bonus " +
                    "from GameObjects z1 ignore index (`PRIMARY`), Ambushes z2, GameObjects z3 ignore index (`PRIMARY`), Caravans z4, Players z5, Players z6 " +
                    "where z2.PGUID=z5.GUID and z4.PGUID=z6.GUID and z5.Race!=z6.Race and z3.Lat between z1.Lat-"+deltaLat+" and z1.Lat+"+deltaLat+" and z3.Lng between z1.Lng-"+deltaLng+" and z1.Lng+"+deltaLng+" and z1.GUID=z2.GUID and z1.Type='Ambush' and z3.Type='Caravan' and z3.GUID=z4.GUID and z2.TTS>=0 and z2.PGUID!=z4.PGUID " +
                    "and z2.Radius>=round(6378137 * acos(cos(z1.Lat / 1e6 * PI() / 180) * cos(z3.Lat / 1e6 * PI() / 180) * cos(z1.Lng / 1e6 * PI() / 180 - z3.Lng / 1e6 * PI() / 180) + sin(z1.Lat / 1e6 * PI() / 180) * sin(z3.Lat / 1e6 * PI() / 180))) order by CGUID");
            rs=query.executeQuery();
            while (rs.next()) {
                CGUID=rs.getString("CGUID");
                if (!CGUID.equals(prevCaravan)) {
                    Start=rs.getString("Start");
                    Finish=rs.getString("Finish");
                    Speed=rs.getInt("Speed");
                    CPGUID = rs.getString("CPGUID");
                    AGUID = rs.getString("AGUID");
                    APGUID = rs.getString("APGUID");
                    ALife = rs.getInt("ALife");
                    AName = rs.getString("AName");
                    SName = rs.getString("SName");
                    FName = rs.getString("FName");
                    SLevel = rs.getInt("SLevel");
                    FLevel = rs.getInt("FLevel");
                    int carBonus = rs.getInt("Bonus");
                    int carDistance = rs.getInt("Distance");
                    ALat = rs.getInt("ALat");
                    ALng = rs.getInt("ALng");
                    PLat = rs.getInt("PLat");
                    PLng = rs.getInt("PLng");
                    Player CPplayer=new Player(CPGUID,con);
                    Player AMBplayer=new Player(APGUID,con);
                    //Bonus = (int) (Math.sqrt(SLevel) * Math.sqrt(FLevel) * Distance*player.getPlayerUpgradeEffect1("cargo")/100);
                    ResultSet rs2;
                    if (Speed>0) {
                        query2=con.prepareStatement("select Lat,Lng from GameObjects z1 where GUID=?");
                        query2.setString(1,Start);
                        rs2=query2.executeQuery();
                        rs2.next();
                        Lat=rs2.getInt("Lat");
                        Lng=rs2.getInt("Lng");
                    }
                    else {
                        query2=con.prepareStatement("select Lat,Lng from GameObjects z1 where GUID=?");
                        query2.setString(1,Finish);
                        rs2=query2.executeQuery();
                        rs2.next();
                        Lat=rs2.getInt("Lat");
                        Lng=rs2.getInt("Lng");
                    }
                    MyUtils.Logwrite("World.handleAmbushes","SName = "+SName+", FName = "+FName+". StartLat,StartLng = "+Lat+","+Lng+". ALat,ALng = "+ALat+","+ALng);
                    Distance=(int)MyUtils.RangeCheck(Lat,Lng,ALat,ALng);
                    //old Bonus=(int) ((Math.sqrt(SLevel) * Math.sqrt(FLevel) * player.getPlayerUpgradeEffect1("cargo")/100) * (5000 + Distance));
                    //Bonus=(int) (((float)(carBonus*10)/carDistance) * (1+(float)(AMBplayer.getPlayerUpgradeEffect2("bargain")/100)) * (3500 + Distance));
                    //Bonus=(int) (((float)(carBonus*10)/carDistance) * (1+(float)(AMBplayer.getPlayerUpgradeEffect2("bargain")/100)) * (3500 + Distance)* (1+(float)(AMBplayer.getPlayerUpgradeEffect2("ambushes")/100)));
                    //Bonus=(int) (3500+ 100*Math.sqrt(Distance)*((((float)carBonus*10)/carDistance) * (1+(float)AMBplayer.getPlayerUpgradeEffect2("bargain")/100)*(1+(float)AMBplayer.getPlayerUpgradeEffect2("ambushes")/100)) );
                    double newCarBonus = Math.pow((1+(double)SLevel/20)*(1+(double)FLevel/20),1.5) * ((double)CPplayer.getPlayerUpgradeEffect1("cargo")/100) * (1+(double)AMBplayer.getPlayerUpgradeEffect2("bargain")/100)*(1+(float)AMBplayer.getPlayerUpgradeEffect2("ambushes")/100);
                    Bonus = (int) (Math.pow(Distance,0.36) * newCarBonus * 500);
                    Caravan caravan = new Caravan(CGUID, CPGUID, SName, FName, PLat, PLng, con);
                    caravan.ambushed(APGUID);
                    Ambush ambush = new Ambush(AGUID, APGUID, AName, ALife, ALat, ALng, con);
                    ambush.caravaned(Bonus);
                    prevCaravan=CGUID;
                }
            }
            query.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("World.handleAmbushes", "SQL Error: " + e.toString());
        }
        MyUtils.Logwrite("World.handleAmbushes", "Finish");
    }
//1
    public void handleFinishedCaravans() {
        PreparedStatement query;
        ResultSet rs;
        int i=0;
        MyUtils.Logwrite("World.handleFinishedCaravans", "Start");
        try {
            query= con.prepareStatement("select z0.GUID, z0.PGUID, z0.Lifetime, z0.Danger, z0.Start,z0.Finish, z0.bonus, z2.Lat as LatS, z2.Lng as LngS, z1.Lat as Lat, z1.Lng as Lng, z3.Lat as LatF, z3.Lng as LngF, z0.Speed " +
                    "from Caravans z0, GameObjects z1, GameObjects z2, GameObjects z3 " +
                    "where z0.GUID=z1.GUID and z0.Start=z2.GUID and z0.Finish=z3.GUID " +
                    "and ( ( (sign(z1.Lat-z3.Lat)*sign(z2.Lat-z3.Lat)<=0) or (sign(z1.Lat-z2.Lat)*sign(z2.Lat-z3.Lat)>=0) ) " +
                    "and ( (sign(z1.Lng-z3.Lng)*sign(z2.Lng-z3.Lng)<=0) or (sign(z1.Lng-z2.Lng)*sign(z2.Lng-z3.Lng)>=0) ) )");
            rs = query.executeQuery();
            while (rs.next()) {
                Caravan caravan = new Caravan (rs.getString("GUID"),rs.getString("PGUID"),rs.getInt("Lifetime"),rs.getInt("Danger"),rs.getString("Start"),rs.getString("Finish"),rs.getInt("bonus"),rs.getInt("Lat"),rs.getInt("Lng"),rs.getInt("LatS"),rs.getInt("LngS"),rs.getInt("LatF"),rs.getInt("LngF"),rs.getInt("Speed"),con);
                if (caravan.Lifetime>=180) {
                    caravan.Danger+=2;
                    caravan.generateAmbush();
                }
                caravan.finish();
                i++;
            }
            query.close();
            con.commit();
        } catch (SQLException e) {
            MyUtils.Logwrite("World.handleFinishedCaravans", "SQL Error: " + e.toString());
        }
        MyUtils.Logwrite("World.handleFinishedCaravans", "Finish. "+Integer.toString(i)+" caravans finished.");
    }

    public void spawn() {
        PreparedStatement query,query2;
        ResultSet rs;
        int i=0;
        MyUtils.Logwrite("World.spawn", "Start");
        try {
            query= con.prepareStatement("select z0.GUID, z1.Lat, z1.Lng from Cities z0, GameObjects z1 where z0.GUID=z1.GUID and z0.Level>1 and z0.tries<25");
            rs = query.executeQuery();
            query2=con.prepareStatement("update Cities set tries=tries+1 where Level>1 and tries<25");
            query2.execute();
            query2.close();
            con.commit();
            while (rs.next()) {
                City city = new City(con);
                city.spawn(rs.getString("GUID"),rs.getInt("Lat"),rs.getInt("Lng"));
            }
            rs.close();
            query.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("World.spawn", "SQL Error: " + e.toString());
        }
        MyUtils.Logwrite("World.spawn", "Finish. "+Integer.toString(i)+" caravans finished.");
    }

    public void generateChests()
    {
        PreparedStatement query,query2;
        ResultSet rs;
        Random random=new Random();
        int i=0;
        int totalCities=0;
        int totalChests=0;
        MyUtils.Logwrite("World.generateChests", "Start");
        try {
            query= con.prepareStatement("select z0.Level, z1.Lat, z1.Lng from Cities z0, GameObjects z1 where z0.GUID=z1.GUID");
            rs = query.executeQuery();
            rs.last();
            totalCities=rs.getRow();
            totalChests=totalCities/10;
            //MyUtils.Logwrite("World.generateChests","totalCities="+totalCities+", totalChests="+totalChests);
            for (i=0;i<=totalChests;i++)
            {
                int choosenCity= random.nextInt(totalCities);
                rs.absolute(choosenCity);
                Chest chest = new Chest(con);
                chest.generate(rs.getInt("Level"),rs.getInt("Lat"),rs.getInt("Lng"));
            }
            query.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("World.generateChests", "SQL Error: " + e.toString());
        }
        MyUtils.Logwrite("World.generateChests", "Создано "+Integer.toString(totalChests)+" сундуков.");
    }

    public void removeOldChests()
    {
        String queryStr;
        SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy");
        Calendar c = new GregorianCalendar();
        c.add(Calendar.DAY_OF_YEAR, -5);
        PreparedStatement query;
        MyUtils.Logwrite("World.removeOldChests", "Start");
        queryStr="delete from tchests where str_to_date(created,'%d.%m.%Y')<STR_TO_DATE('"+dateformat.format(c.getTime())+"','%d.%m.%Y')";
        MyUtils.Logwrite("World.removeOldChests","queryStr= "+queryStr);
        try {
/*            query= con.prepareStatement("delete from tchests where str_to_date(created,'%d.%m.%Y')>?");
            query.setString(1, "STR_TO_DATE("+c.getTime()+",'%d.%m.%Y')");
*/
            query= con.prepareStatement(queryStr);
            query.execute();
            query= con.prepareStatement("delete from GameObjects where Type='Chest' and GUID not in (select GUID from tchests)");
            query.execute();
            query.close();
            con.commit();
        } catch (SQLException e) {
            MyUtils.Logwrite("World.removeOldChests", "c.getTime = "+c.getTime()+", SQL Error: " + e.toString());
        }
        MyUtils.Logwrite("World.removeOldChests", "Finish");
    }

    public void improveBounty() {
        PreparedStatement query;
        try {
            query = con.prepareStatement("update params set value=value+5 where name='bounty'");
            query.execute();
            query.close();
            con.commit();
        }
        catch (SQLException e) {MyUtils.Logwrite("World.improveBounty", "SQL Error: " + e.toString());}
    }

    public static String Destroy() {
        return "Undestroyble";
        /*PreparedStatement query;
        try {
            Connection con = DBUtils.ConnectDB();
            query = con.prepareStatement("truncate table Players");
            query.execute();
            query = con.prepareStatement("truncate table Caravans");
            query.execute();
            query = con.prepareStatement("truncate table Ambushes");
            query.execute();
            query = con.prepareStatement("truncate table GameObjects");
            query.execute();
            query = con.prepareStatement("truncate table PUpgrades");
            query.execute();
            query = con.prepareStatement("truncate table Cities");
            query.execute();
            query = con.prepareStatement("truncate table Connections");
            query.execute();
            query = con.prepareStatement("truncate table Messages");
            query.execute();
            query = con.prepareStatement("truncate table logs");
            query.execute();
            query = con.prepareStatement("update Fractions set Gold=0");
            query.execute();
            query = con.prepareStatement("truncate table Stats");
            query.execute();
            query.close();
            con.commit();
            con.close();
            return "World destroyed. MUHAHAHA!";
        } catch (SQLException |NamingException e) {return "Oops, God protected this world with "+e.toString();}*/
    }

    public static String Create() {
        /*PreparedStatement query;
        String Login,Password, ret="";
        //try {
            //Ростов
            Generate.newGenCity(47307347, 39589577, 47167543, 39893074); //2500
            //Кущевка
            Generate.newGenCity(46584512, 39586229, 46536827, 39680300); //332
            //Азов
            Generate.newGenCity(47122008, 39363756, 47053516, 39474306);
            //Батайск
            Generate.newGenCity(47174544, 39635410, 47063573, 39809474);
            //Таганрог
            Generate.newGenCity(47287147, 38827915, 47171044, 38958721);
            //Новочеркасск
            Generate.newGenCity(47539918, 40016670, 47371849, 40140953);
            //Шахты
            Generate.newGenCity(47775398, 40094688, 47666955, 40367557);
            //Родионовка
            Generate.newGenCity(47626411, 39690386, 47592544, 39735423);
            //Новошахтинск
            Generate.newGenCity(47851379, 39781321, 47716427, 40026110);

            //Талакан
            Generate.newGenCity(59829394, 110898403, 59820852, 110921709);
            //Краснодар
            Generate.newGenCity(45151233, 38873221, 44977501, 39112553);
            //Сочи
            Generate.newGenCity(43666979,39664046 ,43512949 ,39859716 );
            //Адлер
            Generate.newGenCity(43512949 ,39859716, 43386406, 40008288);
            //Волжский
            Generate.newGenCity(48899475 ,44714087, 48737706, 44877058);
            //Сургут
            Generate.newGenCity(61303298 ,73232971, 61230017, 73528915);
            //Павлодар
            Generate.newGenCity(52403985 ,76856997, 52231475, 77071231);
            //Москва
            Generate.newGenCity(55910412 ,37380301, 55578797, 37847906);
            //Тверь
            Generate.newGenCity(56935822 ,35717768, 56783416, 36055941);
            //Кувшиново
            Generate.newGenCity(57052705 ,34100969, 57004085, 34206815);
            //Торжок
            Generate.newGenCity(57079523 ,34923261, 57008536, 35027193);
            //Анапа
            Generate.newGenCity(44980452 ,37259552, 44867295, 37367698);
            //Ставрополь
            Generate.newGenCity(45163014 ,41851203, 44969511, 42089812);
            //Волгоград
            Generate.newGenCity(48890717 ,44378163, 48460466, 44646177);
            //Севастополь
            Generate.newGenCity(44843913 ,33378198, 44386314, 33894804);
            //Майкоп
            Generate.newGenCity(44645017 ,40051725, 44566555, 40156610);
            //Челябинск
            Generate.newGenCity(55322847 ,61229351, 55015772, 61578854);
            //Питер
            Generate.newGenCity(60093809 ,30079921, 59787361, 30558513);
            //Мстиславль
            Generate.newGenCity(54036278 ,31658804, 53998538, 31756818);
            //Симферополь
            Generate.newGenCity(45003123, 34024753, 44890315, 34192462);
            //Нижний Новгород
            Generate.newGenCity(56405287, 43676798, 56182760, 44139597);
            //Клин
            Generate.newGenCity(56379199, 36663374, 56309561, 36778387);
            //Пицунда
            Generate.newGenCity(43240125, 40271764, 43142255, 40462308);
            //Всеволжск
            Generate.newGenCity(60049576, 30586955, 59973900, 30724026);
            //Воронеж
            Generate.newGenCity(51874603, 39011551, 51482454, 39461885);
*/

        //Упс, для общего плана надо переделать генерилку, добавить параметр расстояния между городами. Сейчас он 375 метров
            //Общий план
            //Generate.GenCity(49073865, 37655639, 46096090, 41943054, 332);
            return "Завершили генерацию городов";
       /*     Connection con = DBUtils.ConnectDB();
            query = con.prepareStatement("select Login, Password from Users");
            ResultSet rs=query.executeQuery();
            while (rs.next()){
                Login=rs.getString("Login");
                Password=rs.getString("Password");
                Player player = new Player();
                player.register(Login, Password);
            }
            con.commit();
            con.close();
            return "World created! \n"+ret;
        } catch (SQLException |NamingException e) {return "Dark Force prevents to create this world. "+e.toString()+"\n"+ret;}
        */
    }

    public void checkSurveysFinish() {
        PreparedStatement query;
        ResultSet rs;
        //JSONObject jresult = new JSONObject();
        String messageText;
        String resType;
        try {
            query = con.prepareStatement("select PGUID, lat, lng, type, maxQuantity, currentQuantity from surveys where not done and created<NOW-5/1440");
            rs = query.executeQuery();
            //похерим ли тут отправку сообщения, если между вычиткой и апдейтов будет зазор. надежнее (но медленнее) апедейтить по одной строчке в выборке
            query = con.prepareStatement("update surveys set done=true where not done and created<NOW-5/1440");
            query.execute();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    switch (rs.getString("type")) {
                        case "trees":
                            resType="Дерево";
                            break;
                        case "hills":
                            resType="Камень";
                            break;
                        case "fields":
                            resType="Зерно";
                            break;
                        default:
                            resType="ресурс не определен";
                    }
                    //jresult.put("maxQuantity",rs.getInt("maxQuantity"));
                    //jresult.put("currentQuantity",rs.getInt("currentQuantity"));
                    messageText="Ваши геологи завершили исследование местности!\nРесурс: "+resType+".\nМаксимальное количество: "+rs.getInt("maxQuantity")+".\nТекущее количество:"+rs.getInt("currentQuantity")+".";
                    MyUtils.Message(rs.getString("PGUID"),messageText,5,0,rs.getInt("lat"),rs.getInt("lng"));
                }
            }

            query.close();
            con.commit();
        }
        catch (SQLException e) {MyUtils.Logwrite("World.improveBounty", "SQL Error: " + e.toString());}
    }
}
