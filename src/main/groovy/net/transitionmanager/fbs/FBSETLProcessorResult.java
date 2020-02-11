// automatically generated by the FlatBuffers compiler, do not modify

package net.transitionmanager.fbs;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class FBSETLProcessorResult extends Table {
  public static FBSETLProcessorResult getRootAsFBSETLProcessorResult(ByteBuffer _bb) { return getRootAsFBSETLProcessorResult(_bb, new FBSETLProcessorResult()); }
  public static FBSETLProcessorResult getRootAsFBSETLProcessorResult(ByteBuffer _bb, FBSETLProcessorResult obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public FBSETLProcessorResult __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public FBSETLInfo ETLInfo() { return ETLInfo(new FBSETLInfo()); }
  public FBSETLInfo ETLInfo(FBSETLInfo obj) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  public String consoleLog() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer consoleLogAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer consoleLogInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }
  public FBSDomainResult domains(int j) { return domains(new FBSDomainResult(), j); }
  public FBSDomainResult domains(FBSDomainResult obj, int j) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int domainsLength() { int o = __offset(8); return o != 0 ? __vector_len(o) : 0; }
  public long version() { int o = __offset(10); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }

  public static int createFBSETLProcessorResult(FlatBufferBuilder builder,
      int ETLInfoOffset,
      int consoleLogOffset,
      int domainsOffset,
      long version) {
    builder.startObject(4);
    FBSETLProcessorResult.addVersion(builder, version);
    FBSETLProcessorResult.addDomains(builder, domainsOffset);
    FBSETLProcessorResult.addConsoleLog(builder, consoleLogOffset);
    FBSETLProcessorResult.addETLInfo(builder, ETLInfoOffset);
    return FBSETLProcessorResult.endFBSETLProcessorResult(builder);
  }

  public static void startFBSETLProcessorResult(FlatBufferBuilder builder) { builder.startObject(4); }
  public static void addETLInfo(FlatBufferBuilder builder, int ETLInfoOffset) { builder.addOffset(0, ETLInfoOffset, 0); }
  public static void addConsoleLog(FlatBufferBuilder builder, int consoleLogOffset) { builder.addOffset(1, consoleLogOffset, 0); }
  public static void addDomains(FlatBufferBuilder builder, int domainsOffset) { builder.addOffset(2, domainsOffset, 0); }
  public static int createDomainsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startDomainsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addVersion(FlatBufferBuilder builder, long version) { builder.addLong(3, version, 0L); }
  public static int endFBSETLProcessorResult(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishFBSETLProcessorResultBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
  public static void finishSizePrefixedFBSETLProcessorResultBuffer(FlatBufferBuilder builder, int offset) { builder.finishSizePrefixed(offset); }
}

