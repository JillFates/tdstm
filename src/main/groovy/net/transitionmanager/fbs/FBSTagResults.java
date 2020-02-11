// automatically generated by the FlatBuffers compiler, do not modify

package net.transitionmanager.fbs;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class FBSTagResults extends Table {
  public static FBSTagResults getRootAsFBSTagResults(ByteBuffer _bb) { return getRootAsFBSTagResults(_bb, new FBSTagResults()); }
  public static FBSTagResults getRootAsFBSTagResults(ByteBuffer _bb, FBSTagResults obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public FBSTagResults __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String add(int j) { int o = __offset(4); return o != 0 ? __string(__vector(o) + j * 4) : null; }
  public int addLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public String remove(int j) { int o = __offset(6); return o != 0 ? __string(__vector(o) + j * 4) : null; }
  public int removeLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public FBSTagReplace replace(int j) { return replace(new FBSTagReplace(), j); }
  public FBSTagReplace replace(FBSTagReplace obj, int j) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int replaceLength() { int o = __offset(8); return o != 0 ? __vector_len(o) : 0; }
  public FBSTagReplace replaceByKey(String key) { int o = __offset(8); return o != 0 ? FBSTagReplace.__lookup_by_key(null, __vector(o), key, bb) : null; }
  public FBSTagReplace replaceByKey(FBSTagReplace obj, String key) { int o = __offset(8); return o != 0 ? FBSTagReplace.__lookup_by_key(obj, __vector(o), key, bb) : null; }

  public static int createFBSTagResults(FlatBufferBuilder builder,
      int addOffset,
      int removeOffset,
      int replaceOffset) {
    builder.startObject(3);
    FBSTagResults.addReplace(builder, replaceOffset);
    FBSTagResults.addRemove(builder, removeOffset);
    FBSTagResults.addAdd(builder, addOffset);
    return FBSTagResults.endFBSTagResults(builder);
  }

  public static void startFBSTagResults(FlatBufferBuilder builder) { builder.startObject(3); }
  public static void addAdd(FlatBufferBuilder builder, int addOffset) { builder.addOffset(0, addOffset, 0); }
  public static int createAddVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startAddVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addRemove(FlatBufferBuilder builder, int removeOffset) { builder.addOffset(1, removeOffset, 0); }
  public static int createRemoveVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startRemoveVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addReplace(FlatBufferBuilder builder, int replaceOffset) { builder.addOffset(2, replaceOffset, 0); }
  public static int createReplaceVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startReplaceVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endFBSTagResults(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

