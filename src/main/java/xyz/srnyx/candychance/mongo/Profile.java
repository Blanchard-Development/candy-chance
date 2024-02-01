package xyz.srnyx.candychance.mongo;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.Nullable;


public class Profile {
    @BsonProperty(value = "_id") public ObjectId id;
    public Long user;
    @Nullable public Integer candy;

    public int getCandy() {
        return candy == null ? 0 : candy;
    }
}
