package com.example.conno08.lasttry4;

/**
 * Created by conno08 on 20/04/2015.
 */
public class TrafficInfo
{
    int _id;
    String _location;
    //String _phone_number;;
    public TrafficInfo(){   }
    public TrafficInfo(int id, String _location){
        this._id = id;
        this._location = _location;
        //this._phone_number = _phone_number;
    }

    public TrafficInfo(String _location){
        this._location = _location;
        //this._phone_number = _phone_number;
    }
    public int getID(){
        return this._id;
    }

    public void setID(int id){
        this._id = id;
    }

    public String getLocation(){
        return this._location;
    }

    public void setLocation(String location){
        this._location = location;
    }

    /*public String getPhoneNumber(){
        return this._phone_number;
    }

    public void setPhoneNumber(String phone_number){
        this._phone_number = phone_number;
    }*/
}
