<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.Player"%>
<%
Boolean check=true;
String result;
String token=request.getParameter("Token");
if (token == null) check=false;
String ReqName=request.getParameter("ReqName");
if (ReqName == null) check=false;
String LatS=request.getParameter("lat");
if (LatS == null) LatS="100";
String LngS=request.getParameter("lng");
if (LngS == null) LngS="200";
String PLatS=request.getParameter("plat");
if (PLatS == null) PLatS="100";
String PLngS=request.getParameter("plng");
if (PLngS == null) PLngS="200";
String TGUID=request.getParameter("TGUID");
if (TGUID == null) TGUID="";
String text=request.getParameter("text");
if (text == null) text="";

String ItemType=request.getParameter("Type");
if (ItemType == null) ItemType="";

String QuantityS=request.getParameter("Quantity");
if (QuantityS == null) QuantityS="0";
int Quantity=Integer.parseInt(QuantityS);

String SclientTime=request.getParameter("clientTime");
if (SclientTime == null) SclientTime="0";


String sGold=request.getParameter("Gold");
if (sGold == null) sGold="0";
String sObsidian=request.getParameter("Obsidian");
if (sObsidian == null) sObsidian="0";

int Gold=Integer.parseInt(sGold);
int Obsidian=Integer.parseInt(sObsidian);

long clientTime=Long.parseLong(SclientTime);
int PLAT=Integer.parseInt(PLatS);
int PLNG=Integer.parseInt(PLngS);
int LAT=Integer.parseInt(LatS);
int LNG=Integer.parseInt(LngS);
String PRace=request.getParameter("Race");
if (PRace == null) PRace="0";
int RACE=Integer.parseInt(PRace);
String AMOUNTS=request.getParameter("Amount");
if (AMOUNTS == null) AMOUNTS="0";
int AMOUNT=Integer.parseInt(AMOUNTS);
//check=false;
if (check) {
Player player=new Player(token, PLAT, PLNG);
result = player.sendData(ReqName,TGUID,LAT,LNG,RACE,AMOUNT,text,ItemType,Quantity,clientTime,Gold,Obsidian);
}
else {
result="Check parameters";
}
//result="Технические работы на сервере."
%>
<%=result%>