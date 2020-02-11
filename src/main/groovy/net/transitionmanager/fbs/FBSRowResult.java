// automatically generated by the FlatBuffers compiler, do not modify

package net.transitionmanager.fbs;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class FBSRowResult extends Table {
  public static FBSRowResult getRootAsFBSRowResult(ByteBuffer _bb) { return getRootAsFBSRowResult(_bb, new FBSRowResult()); }
  public static FBSRowResult getRootAsFBSRowResult(ByteBuffer _bb, FBSRowResult obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public FBSRowResult __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String op() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer opAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer opInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public int rowNum() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int errorCount() { int o = __offset(8); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public boolean warn() { int o = __offset(10); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }
  public boolean duplicate() { int o = __offset(12); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }
  public String errors(int j) { int o = __offset(14); return o != 0 ? __string(__vector(o) + j * 4) : null; }
  public int errorsLength() { int o = __offset(14); return o != 0 ? __vector_len(o) : 0; }
  public boolean ignore() { int o = __offset(16); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }
  public FBSFieldResult fields(int j) { return fields(new FBSFieldResult(), j); }
  public FBSFieldResult fields(FBSFieldResult obj, int j) { int o = __offset(18); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int fieldsLength() { int o = __offset(18); return o != 0 ? __vector_len(o) : 0; }
  public FBSFieldResult fieldsByKey(String key) { int o = __offset(18); return o != 0 ? FBSFieldResult.__lookup_by_key(null, __vector(o), key, bb) : null; }
  public FBSFieldResult fieldsByKey(FBSFieldResult obj, String key) { int o = __offset(18); return o != 0 ? FBSFieldResult.__lookup_by_key(obj, __vector(o), key, bb) : null; }
  public String domain() { int o = __offset(20); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer domainAsByteBuffer() { return __vector_as_bytebuffer(20, 1); }
  public ByteBuffer domainInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 20, 1); }
  public String comments(int j) { int o = __offset(22); return o != 0 ? __string(__vector(o) + j * 4) : null; }
  public int commentsLength() { int o = __offset(22); return o != 0 ? __vector_len(o) : 0; }
  public FBSTagResults tags(int j) { return tags(new FBSTagResults(), j); }
  public FBSTagResults tags(FBSTagResults obj, int j) { int o = __offset(24); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int tagsLength() { int o = __offset(24); return o != 0 ? __vector_len(o) : 0; }

  public static int createFBSRowResult(FlatBufferBuilder builder,
      int opOffset,
      int rowNum,
      int errorCount,
      boolean warn,
      boolean duplicate,
      int errorsOffset,
      boolean ignore,
      int fieldsOffset,
      int domainOffset,
      int commentsOffset,
      int tagsOffset) {
    builder.startObject(11);
    FBSRowResult.addTags(builder, tagsOffset);
    FBSRowResult.addComments(builder, commentsOffset);
    FBSRowResult.addDomain(builder, domainOffset);
    FBSRowResult.addFields(builder, fieldsOffset);
    FBSRowResult.addErrors(builder, errorsOffset);
    FBSRowResult.addErrorCount(builder, errorCount);
    FBSRowResult.addRowNum(builder, rowNum);
    FBSRowResult.addOp(builder, opOffset);
    FBSRowResult.addIgnore(builder, ignore);
    FBSRowResult.addDuplicate(builder, duplicate);
    FBSRowResult.addWarn(builder, warn);
    return FBSRowResult.endFBSRowResult(builder);
  }

  public static void startFBSRowResult(FlatBufferBuilder builder) { builder.startObject(11); }
  public static void addOp(FlatBufferBuilder builder, int opOffset) { builder.addOffset(0, opOffset, 0); }
  public static void addRowNum(FlatBufferBuilder builder, int rowNum) { builder.addInt(1, rowNum, 0); }
  public static void addErrorCount(FlatBufferBuilder builder, int errorCount) { builder.addInt(2, errorCount, 0); }
  public static void addWarn(FlatBufferBuilder builder, boolean warn) { builder.addBoolean(3, warn, false); }
  public static void addDuplicate(FlatBufferBuilder builder, boolean duplicate) { builder.addBoolean(4, duplicate, false); }
  public static void addErrors(FlatBufferBuilder builder, int errorsOffset) { builder.addOffset(5, errorsOffset, 0); }
  public static int createErrorsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startErrorsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addIgnore(FlatBufferBuilder builder, boolean ignore) { builder.addBoolean(6, ignore, false); }
  public static void addFields(FlatBufferBuilder builder, int fieldsOffset) { builder.addOffset(7, fieldsOffset, 0); }
  public static int createFieldsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startFieldsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addDomain(FlatBufferBuilder builder, int domainOffset) { builder.addOffset(8, domainOffset, 0); }
  public static void addComments(FlatBufferBuilder builder, int commentsOffset) { builder.addOffset(9, commentsOffset, 0); }
  public static int createCommentsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startCommentsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addTags(FlatBufferBuilder builder, int tagsOffset) { builder.addOffset(10, tagsOffset, 0); }
  public static int createTagsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startTagsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endFBSRowResult(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

