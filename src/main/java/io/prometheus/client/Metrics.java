package io.prometheus.client;

public final class Metrics {
  private Metrics() {}
  public static void registerAllExtensions(
          com.google.protobuf.ExtensionRegistry registry) {
  }
  /**
   * Protobuf enum {@code io.prometheus.client.MetricType}
   */
  public enum MetricType
          implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>COUNTER = 0;</code>
     */
    COUNTER(0, 0),
    /**
     * <code>GAUGE = 1;</code>
     */
    GAUGE(1, 1),
    /**
     * <code>SUMMARY = 2;</code>
     */
    SUMMARY(2, 2),
    /**
     * <code>UNTYPED = 3;</code>
     */
    UNTYPED(3, 3),
    /**
     * <code>HISTOGRAM = 4;</code>
     */
    HISTOGRAM(4, 4),
    ;

    /**
     * <code>COUNTER = 0;</code>
     */
    public static final int COUNTER_VALUE = 0;
    /**
     * <code>GAUGE = 1;</code>
     */
    public static final int GAUGE_VALUE = 1;
    /**
     * <code>SUMMARY = 2;</code>
     */
    public static final int SUMMARY_VALUE = 2;
    /**
     * <code>UNTYPED = 3;</code>
     */
    public static final int UNTYPED_VALUE = 3;
    /**
     * <code>HISTOGRAM = 4;</code>
     */
    public static final int HISTOGRAM_VALUE = 4;


    public final int getNumber() { return value; }

    public static MetricType valueOf(int value) {
      switch (value) {
        case 0: return COUNTER;
        case 1: return GAUGE;
        case 2: return SUMMARY;
        case 3: return UNTYPED;
        case 4: return HISTOGRAM;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<MetricType>
    internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<MetricType>
            internalValueMap =
            new com.google.protobuf.Internal.EnumLiteMap<MetricType>() {
              public MetricType findValueByNumber(int number) {
                return MetricType.valueOf(number);
              }
            };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
    getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
    getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.getDescriptor().getEnumTypes().get(0);
    }

    private static final MetricType[] VALUES = values();

    public static MetricType valueOf(
            com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new IllegalArgumentException(
                "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }

    private final int index;
    private final int value;

    private MetricType(int index, int value) {
      this.index = index;
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:io.prometheus.client.MetricType)
  }

  public interface LabelPairOrBuilder extends
          // @@protoc_insertion_point(interface_extends:io.prometheus.client.LabelPair)
          com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional string name = 1;</code>
     */
    boolean hasName();
    /**
     * <code>optional string name = 1;</code>
     */
    String getName();
    /**
     * <code>optional string name = 1;</code>
     */
    com.google.protobuf.ByteString
    getNameBytes();

    /**
     * <code>optional string value = 2;</code>
     */
    boolean hasValue();
    /**
     * <code>optional string value = 2;</code>
     */
    String getValue();
    /**
     * <code>optional string value = 2;</code>
     */
    com.google.protobuf.ByteString
    getValueBytes();
  }
  /**
   * Protobuf type {@code io.prometheus.client.LabelPair}
   */
  public static final class LabelPair extends
          com.google.protobuf.GeneratedMessage implements
          // @@protoc_insertion_point(message_implements:io.prometheus.client.LabelPair)
          LabelPairOrBuilder {
    // Use LabelPair.newBuilder() to construct.
    private LabelPair(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private LabelPair(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final LabelPair defaultInstance;
    public static LabelPair getDefaultInstance() {
      return defaultInstance;
    }

    public LabelPair getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private LabelPair(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
              com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                      extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000001;
              name_ = bs;
              break;
            }
            case 18: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000002;
              value_ = bs;
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_LabelPair_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_LabelPair_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      io.prometheus.client.Metrics.LabelPair.class, io.prometheus.client.Metrics.LabelPair.Builder.class);
    }

    public static com.google.protobuf.Parser<LabelPair> PARSER =
            new com.google.protobuf.AbstractParser<LabelPair>() {
              public LabelPair parsePartialFrom(
                      com.google.protobuf.CodedInputStream input,
                      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                      throws com.google.protobuf.InvalidProtocolBufferException {
                return new LabelPair(input, extensionRegistry);
              }
            };

    @Override
    public com.google.protobuf.Parser<LabelPair> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int NAME_FIELD_NUMBER = 1;
    private Object name_;
    /**
     * <code>optional string name = 1;</code>
     */
    public boolean hasName() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional string name = 1;</code>
     */
    public String getName() {
      Object ref = name_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          name_ = s;
        }
        return s;
      }
    }
    /**
     * <code>optional string name = 1;</code>
     */
    public com.google.protobuf.ByteString
    getNameBytes() {
      Object ref = name_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (String) ref);
        name_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int VALUE_FIELD_NUMBER = 2;
    private Object value_;
    /**
     * <code>optional string value = 2;</code>
     */
    public boolean hasValue() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional string value = 2;</code>
     */
    public String getValue() {
      Object ref = value_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          value_ = s;
        }
        return s;
      }
    }
    /**
     * <code>optional string value = 2;</code>
     */
    public com.google.protobuf.ByteString
    getValueBytes() {
      Object ref = value_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (String) ref);
        value_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private void initFields() {
      name_ = "";
      value_ = "";
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getNameBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getValueBytes());
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
                .computeBytesSize(1, getNameBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
                .computeBytesSize(2, getValueBytes());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
            throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static io.prometheus.client.Metrics.LabelPair parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.LabelPair parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.LabelPair parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.LabelPair parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.LabelPair parseFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.LabelPair parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.LabelPair parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static io.prometheus.client.Metrics.LabelPair parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.LabelPair parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.LabelPair parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(io.prometheus.client.Metrics.LabelPair prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code io.prometheus.client.LabelPair}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessage.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:io.prometheus.client.LabelPair)
            io.prometheus.client.Metrics.LabelPairOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_LabelPair_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_LabelPair_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        io.prometheus.client.Metrics.LabelPair.class, io.prometheus.client.Metrics.LabelPair.Builder.class);
      }

      // Construct using io.prometheus.client.Metrics.LabelPair.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
              com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        name_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        value_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
      getDescriptorForType() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_LabelPair_descriptor;
      }

      public io.prometheus.client.Metrics.LabelPair getDefaultInstanceForType() {
        return io.prometheus.client.Metrics.LabelPair.getDefaultInstance();
      }

      public io.prometheus.client.Metrics.LabelPair build() {
        io.prometheus.client.Metrics.LabelPair result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public io.prometheus.client.Metrics.LabelPair buildPartial() {
        io.prometheus.client.Metrics.LabelPair result = new io.prometheus.client.Metrics.LabelPair(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.name_ = name_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.value_ = value_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.prometheus.client.Metrics.LabelPair) {
          return mergeFrom((io.prometheus.client.Metrics.LabelPair)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.prometheus.client.Metrics.LabelPair other) {
        if (other == io.prometheus.client.Metrics.LabelPair.getDefaultInstance()) return this;
        if (other.hasName()) {
          bitField0_ |= 0x00000001;
          name_ = other.name_;
          onChanged();
        }
        if (other.hasValue()) {
          bitField0_ |= 0x00000002;
          value_ = other.value_;
          onChanged();
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws java.io.IOException {
        io.prometheus.client.Metrics.LabelPair parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.prometheus.client.Metrics.LabelPair) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private Object name_ = "";
      /**
       * <code>optional string name = 1;</code>
       */
      public boolean hasName() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional string name = 1;</code>
       */
      public String getName() {
        Object ref = name_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
                  (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            name_ = s;
          }
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>optional string name = 1;</code>
       */
      public com.google.protobuf.ByteString
      getNameBytes() {
        Object ref = name_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b =
                  com.google.protobuf.ByteString.copyFromUtf8(
                          (String) ref);
          name_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string name = 1;</code>
       */
      public Builder setName(
              String value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000001;
        name_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional string name = 1;</code>
       */
      public Builder clearName() {
        bitField0_ = (bitField0_ & ~0x00000001);
        name_ = getDefaultInstance().getName();
        onChanged();
        return this;
      }
      /**
       * <code>optional string name = 1;</code>
       */
      public Builder setNameBytes(
              com.google.protobuf.ByteString value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000001;
        name_ = value;
        onChanged();
        return this;
      }

      private Object value_ = "";
      /**
       * <code>optional string value = 2;</code>
       */
      public boolean hasValue() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional string value = 2;</code>
       */
      public String getValue() {
        Object ref = value_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
                  (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            value_ = s;
          }
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>optional string value = 2;</code>
       */
      public com.google.protobuf.ByteString
      getValueBytes() {
        Object ref = value_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b =
                  com.google.protobuf.ByteString.copyFromUtf8(
                          (String) ref);
          value_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string value = 2;</code>
       */
      public Builder setValue(
              String value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000002;
        value_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional string value = 2;</code>
       */
      public Builder clearValue() {
        bitField0_ = (bitField0_ & ~0x00000002);
        value_ = getDefaultInstance().getValue();
        onChanged();
        return this;
      }
      /**
       * <code>optional string value = 2;</code>
       */
      public Builder setValueBytes(
              com.google.protobuf.ByteString value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000002;
        value_ = value;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:io.prometheus.client.LabelPair)
    }

    static {
      defaultInstance = new LabelPair(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:io.prometheus.client.LabelPair)
  }

  public interface GaugeOrBuilder extends
          // @@protoc_insertion_point(interface_extends:io.prometheus.client.Gauge)
          com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional double value = 1;</code>
     */
    boolean hasValue();
    /**
     * <code>optional double value = 1;</code>
     */
    double getValue();
  }
  /**
   * Protobuf type {@code io.prometheus.client.Gauge}
   */
  public static final class Gauge extends
          com.google.protobuf.GeneratedMessage implements
          // @@protoc_insertion_point(message_implements:io.prometheus.client.Gauge)
          GaugeOrBuilder {
    // Use Gauge.newBuilder() to construct.
    private Gauge(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Gauge(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Gauge defaultInstance;
    public static Gauge getDefaultInstance() {
      return defaultInstance;
    }

    public Gauge getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Gauge(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
              com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                      extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 9: {
              bitField0_ |= 0x00000001;
              value_ = input.readDouble();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Gauge_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Gauge_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      io.prometheus.client.Metrics.Gauge.class, io.prometheus.client.Metrics.Gauge.Builder.class);
    }

    public static com.google.protobuf.Parser<Gauge> PARSER =
            new com.google.protobuf.AbstractParser<Gauge>() {
              public Gauge parsePartialFrom(
                      com.google.protobuf.CodedInputStream input,
                      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                      throws com.google.protobuf.InvalidProtocolBufferException {
                return new Gauge(input, extensionRegistry);
              }
            };

    @Override
    public com.google.protobuf.Parser<Gauge> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int VALUE_FIELD_NUMBER = 1;
    private double value_;
    /**
     * <code>optional double value = 1;</code>
     */
    public boolean hasValue() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional double value = 1;</code>
     */
    public double getValue() {
      return value_;
    }

    private void initFields() {
      value_ = 0D;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeDouble(1, value_);
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
                .computeDoubleSize(1, value_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
            throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static io.prometheus.client.Metrics.Gauge parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Gauge parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Gauge parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Gauge parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Gauge parseFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Gauge parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Gauge parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static io.prometheus.client.Metrics.Gauge parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Gauge parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Gauge parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(io.prometheus.client.Metrics.Gauge prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code io.prometheus.client.Gauge}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessage.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:io.prometheus.client.Gauge)
            io.prometheus.client.Metrics.GaugeOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Gauge_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Gauge_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        io.prometheus.client.Metrics.Gauge.class, io.prometheus.client.Metrics.Gauge.Builder.class);
      }

      // Construct using io.prometheus.client.Metrics.Gauge.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
              com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        value_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
      getDescriptorForType() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Gauge_descriptor;
      }

      public io.prometheus.client.Metrics.Gauge getDefaultInstanceForType() {
        return io.prometheus.client.Metrics.Gauge.getDefaultInstance();
      }

      public io.prometheus.client.Metrics.Gauge build() {
        io.prometheus.client.Metrics.Gauge result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public io.prometheus.client.Metrics.Gauge buildPartial() {
        io.prometheus.client.Metrics.Gauge result = new io.prometheus.client.Metrics.Gauge(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.value_ = value_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.prometheus.client.Metrics.Gauge) {
          return mergeFrom((io.prometheus.client.Metrics.Gauge)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.prometheus.client.Metrics.Gauge other) {
        if (other == io.prometheus.client.Metrics.Gauge.getDefaultInstance()) return this;
        if (other.hasValue()) {
          setValue(other.getValue());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws java.io.IOException {
        io.prometheus.client.Metrics.Gauge parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.prometheus.client.Metrics.Gauge) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private double value_ ;
      /**
       * <code>optional double value = 1;</code>
       */
      public boolean hasValue() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional double value = 1;</code>
       */
      public double getValue() {
        return value_;
      }
      /**
       * <code>optional double value = 1;</code>
       */
      public Builder setValue(double value) {
        bitField0_ |= 0x00000001;
        value_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double value = 1;</code>
       */
      public Builder clearValue() {
        bitField0_ = (bitField0_ & ~0x00000001);
        value_ = 0D;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:io.prometheus.client.Gauge)
    }

    static {
      defaultInstance = new Gauge(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:io.prometheus.client.Gauge)
  }

  public interface CounterOrBuilder extends
          // @@protoc_insertion_point(interface_extends:io.prometheus.client.Counter)
          com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional double value = 1;</code>
     */
    boolean hasValue();
    /**
     * <code>optional double value = 1;</code>
     */
    double getValue();
  }
  /**
   * Protobuf type {@code io.prometheus.client.Counter}
   */
  public static final class Counter extends
          com.google.protobuf.GeneratedMessage implements
          // @@protoc_insertion_point(message_implements:io.prometheus.client.Counter)
          CounterOrBuilder {
    // Use Counter.newBuilder() to construct.
    private Counter(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Counter(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Counter defaultInstance;
    public static Counter getDefaultInstance() {
      return defaultInstance;
    }

    public Counter getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Counter(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
              com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                      extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 9: {
              bitField0_ |= 0x00000001;
              value_ = input.readDouble();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Counter_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Counter_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      io.prometheus.client.Metrics.Counter.class, io.prometheus.client.Metrics.Counter.Builder.class);
    }

    public static com.google.protobuf.Parser<Counter> PARSER =
            new com.google.protobuf.AbstractParser<Counter>() {
              public Counter parsePartialFrom(
                      com.google.protobuf.CodedInputStream input,
                      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                      throws com.google.protobuf.InvalidProtocolBufferException {
                return new Counter(input, extensionRegistry);
              }
            };

    @Override
    public com.google.protobuf.Parser<Counter> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int VALUE_FIELD_NUMBER = 1;
    private double value_;
    /**
     * <code>optional double value = 1;</code>
     */
    public boolean hasValue() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional double value = 1;</code>
     */
    public double getValue() {
      return value_;
    }

    private void initFields() {
      value_ = 0D;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeDouble(1, value_);
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
                .computeDoubleSize(1, value_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
            throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static io.prometheus.client.Metrics.Counter parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Counter parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Counter parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Counter parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Counter parseFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Counter parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Counter parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static io.prometheus.client.Metrics.Counter parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Counter parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Counter parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(io.prometheus.client.Metrics.Counter prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code io.prometheus.client.Counter}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessage.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:io.prometheus.client.Counter)
            io.prometheus.client.Metrics.CounterOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Counter_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Counter_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        io.prometheus.client.Metrics.Counter.class, io.prometheus.client.Metrics.Counter.Builder.class);
      }

      // Construct using io.prometheus.client.Metrics.Counter.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
              com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        value_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
      getDescriptorForType() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Counter_descriptor;
      }

      public io.prometheus.client.Metrics.Counter getDefaultInstanceForType() {
        return io.prometheus.client.Metrics.Counter.getDefaultInstance();
      }

      public io.prometheus.client.Metrics.Counter build() {
        io.prometheus.client.Metrics.Counter result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public io.prometheus.client.Metrics.Counter buildPartial() {
        io.prometheus.client.Metrics.Counter result = new io.prometheus.client.Metrics.Counter(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.value_ = value_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.prometheus.client.Metrics.Counter) {
          return mergeFrom((io.prometheus.client.Metrics.Counter)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.prometheus.client.Metrics.Counter other) {
        if (other == io.prometheus.client.Metrics.Counter.getDefaultInstance()) return this;
        if (other.hasValue()) {
          setValue(other.getValue());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws java.io.IOException {
        io.prometheus.client.Metrics.Counter parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.prometheus.client.Metrics.Counter) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private double value_ ;
      /**
       * <code>optional double value = 1;</code>
       */
      public boolean hasValue() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional double value = 1;</code>
       */
      public double getValue() {
        return value_;
      }
      /**
       * <code>optional double value = 1;</code>
       */
      public Builder setValue(double value) {
        bitField0_ |= 0x00000001;
        value_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double value = 1;</code>
       */
      public Builder clearValue() {
        bitField0_ = (bitField0_ & ~0x00000001);
        value_ = 0D;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:io.prometheus.client.Counter)
    }

    static {
      defaultInstance = new Counter(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:io.prometheus.client.Counter)
  }

  public interface QuantileOrBuilder extends
          // @@protoc_insertion_point(interface_extends:io.prometheus.client.Quantile)
          com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional double quantile = 1;</code>
     */
    boolean hasQuantile();
    /**
     * <code>optional double quantile = 1;</code>
     */
    double getQuantile();

    /**
     * <code>optional double value = 2;</code>
     */
    boolean hasValue();
    /**
     * <code>optional double value = 2;</code>
     */
    double getValue();
  }
  /**
   * Protobuf type {@code io.prometheus.client.Quantile}
   */
  public static final class Quantile extends
          com.google.protobuf.GeneratedMessage implements
          // @@protoc_insertion_point(message_implements:io.prometheus.client.Quantile)
          QuantileOrBuilder {
    // Use Quantile.newBuilder() to construct.
    private Quantile(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Quantile(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Quantile defaultInstance;
    public static Quantile getDefaultInstance() {
      return defaultInstance;
    }

    public Quantile getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Quantile(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
              com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                      extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 9: {
              bitField0_ |= 0x00000001;
              quantile_ = input.readDouble();
              break;
            }
            case 17: {
              bitField0_ |= 0x00000002;
              value_ = input.readDouble();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Quantile_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Quantile_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      io.prometheus.client.Metrics.Quantile.class, io.prometheus.client.Metrics.Quantile.Builder.class);
    }

    public static com.google.protobuf.Parser<Quantile> PARSER =
            new com.google.protobuf.AbstractParser<Quantile>() {
              public Quantile parsePartialFrom(
                      com.google.protobuf.CodedInputStream input,
                      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                      throws com.google.protobuf.InvalidProtocolBufferException {
                return new Quantile(input, extensionRegistry);
              }
            };

    @Override
    public com.google.protobuf.Parser<Quantile> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int QUANTILE_FIELD_NUMBER = 1;
    private double quantile_;
    /**
     * <code>optional double quantile = 1;</code>
     */
    public boolean hasQuantile() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional double quantile = 1;</code>
     */
    public double getQuantile() {
      return quantile_;
    }

    public static final int VALUE_FIELD_NUMBER = 2;
    private double value_;
    /**
     * <code>optional double value = 2;</code>
     */
    public boolean hasValue() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional double value = 2;</code>
     */
    public double getValue() {
      return value_;
    }

    private void initFields() {
      quantile_ = 0D;
      value_ = 0D;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeDouble(1, quantile_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeDouble(2, value_);
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
                .computeDoubleSize(1, quantile_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
                .computeDoubleSize(2, value_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
            throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static io.prometheus.client.Metrics.Quantile parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Quantile parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Quantile parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Quantile parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Quantile parseFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Quantile parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Quantile parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static io.prometheus.client.Metrics.Quantile parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Quantile parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Quantile parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(io.prometheus.client.Metrics.Quantile prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code io.prometheus.client.Quantile}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessage.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:io.prometheus.client.Quantile)
            io.prometheus.client.Metrics.QuantileOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Quantile_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Quantile_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        io.prometheus.client.Metrics.Quantile.class, io.prometheus.client.Metrics.Quantile.Builder.class);
      }

      // Construct using io.prometheus.client.Metrics.Quantile.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
              com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        quantile_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000001);
        value_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
      getDescriptorForType() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Quantile_descriptor;
      }

      public io.prometheus.client.Metrics.Quantile getDefaultInstanceForType() {
        return io.prometheus.client.Metrics.Quantile.getDefaultInstance();
      }

      public io.prometheus.client.Metrics.Quantile build() {
        io.prometheus.client.Metrics.Quantile result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public io.prometheus.client.Metrics.Quantile buildPartial() {
        io.prometheus.client.Metrics.Quantile result = new io.prometheus.client.Metrics.Quantile(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.quantile_ = quantile_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.value_ = value_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.prometheus.client.Metrics.Quantile) {
          return mergeFrom((io.prometheus.client.Metrics.Quantile)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.prometheus.client.Metrics.Quantile other) {
        if (other == io.prometheus.client.Metrics.Quantile.getDefaultInstance()) return this;
        if (other.hasQuantile()) {
          setQuantile(other.getQuantile());
        }
        if (other.hasValue()) {
          setValue(other.getValue());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws java.io.IOException {
        io.prometheus.client.Metrics.Quantile parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.prometheus.client.Metrics.Quantile) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private double quantile_ ;
      /**
       * <code>optional double quantile = 1;</code>
       */
      public boolean hasQuantile() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional double quantile = 1;</code>
       */
      public double getQuantile() {
        return quantile_;
      }
      /**
       * <code>optional double quantile = 1;</code>
       */
      public Builder setQuantile(double value) {
        bitField0_ |= 0x00000001;
        quantile_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double quantile = 1;</code>
       */
      public Builder clearQuantile() {
        bitField0_ = (bitField0_ & ~0x00000001);
        quantile_ = 0D;
        onChanged();
        return this;
      }

      private double value_ ;
      /**
       * <code>optional double value = 2;</code>
       */
      public boolean hasValue() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional double value = 2;</code>
       */
      public double getValue() {
        return value_;
      }
      /**
       * <code>optional double value = 2;</code>
       */
      public Builder setValue(double value) {
        bitField0_ |= 0x00000002;
        value_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double value = 2;</code>
       */
      public Builder clearValue() {
        bitField0_ = (bitField0_ & ~0x00000002);
        value_ = 0D;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:io.prometheus.client.Quantile)
    }

    static {
      defaultInstance = new Quantile(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:io.prometheus.client.Quantile)
  }

  public interface SummaryOrBuilder extends
          // @@protoc_insertion_point(interface_extends:io.prometheus.client.Summary)
          com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional uint64 sample_count = 1;</code>
     */
    boolean hasSampleCount();
    /**
     * <code>optional uint64 sample_count = 1;</code>
     */
    long getSampleCount();

    /**
     * <code>optional double sample_sum = 2;</code>
     */
    boolean hasSampleSum();
    /**
     * <code>optional double sample_sum = 2;</code>
     */
    double getSampleSum();

    /**
     * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
     */
    java.util.List<Quantile>
    getQuantileList();
    /**
     * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
     */
    io.prometheus.client.Metrics.Quantile getQuantile(int index);
    /**
     * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
     */
    int getQuantileCount();
    /**
     * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
     */
    java.util.List<? extends QuantileOrBuilder>
    getQuantileOrBuilderList();
    /**
     * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
     */
    io.prometheus.client.Metrics.QuantileOrBuilder getQuantileOrBuilder(
            int index);
  }
  /**
   * Protobuf type {@code io.prometheus.client.Summary}
   */
  public static final class Summary extends
          com.google.protobuf.GeneratedMessage implements
          // @@protoc_insertion_point(message_implements:io.prometheus.client.Summary)
          SummaryOrBuilder {
    // Use Summary.newBuilder() to construct.
    private Summary(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Summary(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Summary defaultInstance;
    public static Summary getDefaultInstance() {
      return defaultInstance;
    }

    public Summary getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Summary(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
              com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                      extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              sampleCount_ = input.readUInt64();
              break;
            }
            case 17: {
              bitField0_ |= 0x00000002;
              sampleSum_ = input.readDouble();
              break;
            }
            case 26: {
              if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
                quantile_ = new java.util.ArrayList<Quantile>();
                mutable_bitField0_ |= 0x00000004;
              }
              quantile_.add(input.readMessage(io.prometheus.client.Metrics.Quantile.PARSER, extensionRegistry));
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
          quantile_ = java.util.Collections.unmodifiableList(quantile_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Summary_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Summary_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      io.prometheus.client.Metrics.Summary.class, io.prometheus.client.Metrics.Summary.Builder.class);
    }

    public static com.google.protobuf.Parser<Summary> PARSER =
            new com.google.protobuf.AbstractParser<Summary>() {
              public Summary parsePartialFrom(
                      com.google.protobuf.CodedInputStream input,
                      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                      throws com.google.protobuf.InvalidProtocolBufferException {
                return new Summary(input, extensionRegistry);
              }
            };

    @Override
    public com.google.protobuf.Parser<Summary> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int SAMPLE_COUNT_FIELD_NUMBER = 1;
    private long sampleCount_;
    /**
     * <code>optional uint64 sample_count = 1;</code>
     */
    public boolean hasSampleCount() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional uint64 sample_count = 1;</code>
     */
    public long getSampleCount() {
      return sampleCount_;
    }

    public static final int SAMPLE_SUM_FIELD_NUMBER = 2;
    private double sampleSum_;
    /**
     * <code>optional double sample_sum = 2;</code>
     */
    public boolean hasSampleSum() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional double sample_sum = 2;</code>
     */
    public double getSampleSum() {
      return sampleSum_;
    }

    public static final int QUANTILE_FIELD_NUMBER = 3;
    private java.util.List<Quantile> quantile_;
    /**
     * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
     */
    public java.util.List<Quantile> getQuantileList() {
      return quantile_;
    }
    /**
     * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
     */
    public java.util.List<? extends QuantileOrBuilder>
    getQuantileOrBuilderList() {
      return quantile_;
    }
    /**
     * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
     */
    public int getQuantileCount() {
      return quantile_.size();
    }
    /**
     * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
     */
    public io.prometheus.client.Metrics.Quantile getQuantile(int index) {
      return quantile_.get(index);
    }
    /**
     * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
     */
    public io.prometheus.client.Metrics.QuantileOrBuilder getQuantileOrBuilder(
            int index) {
      return quantile_.get(index);
    }

    private void initFields() {
      sampleCount_ = 0L;
      sampleSum_ = 0D;
      quantile_ = java.util.Collections.emptyList();
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeUInt64(1, sampleCount_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeDouble(2, sampleSum_);
      }
      for (int i = 0; i < quantile_.size(); i++) {
        output.writeMessage(3, quantile_.get(i));
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
                .computeUInt64Size(1, sampleCount_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
                .computeDoubleSize(2, sampleSum_);
      }
      for (int i = 0; i < quantile_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(3, quantile_.get(i));
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
            throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static io.prometheus.client.Metrics.Summary parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Summary parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Summary parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Summary parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Summary parseFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Summary parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Summary parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static io.prometheus.client.Metrics.Summary parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Summary parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Summary parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(io.prometheus.client.Metrics.Summary prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code io.prometheus.client.Summary}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessage.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:io.prometheus.client.Summary)
            io.prometheus.client.Metrics.SummaryOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Summary_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Summary_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        io.prometheus.client.Metrics.Summary.class, io.prometheus.client.Metrics.Summary.Builder.class);
      }

      // Construct using io.prometheus.client.Metrics.Summary.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
              com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          getQuantileFieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        sampleCount_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000001);
        sampleSum_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000002);
        if (quantileBuilder_ == null) {
          quantile_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000004);
        } else {
          quantileBuilder_.clear();
        }
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
      getDescriptorForType() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Summary_descriptor;
      }

      public io.prometheus.client.Metrics.Summary getDefaultInstanceForType() {
        return io.prometheus.client.Metrics.Summary.getDefaultInstance();
      }

      public io.prometheus.client.Metrics.Summary build() {
        io.prometheus.client.Metrics.Summary result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public io.prometheus.client.Metrics.Summary buildPartial() {
        io.prometheus.client.Metrics.Summary result = new io.prometheus.client.Metrics.Summary(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.sampleCount_ = sampleCount_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.sampleSum_ = sampleSum_;
        if (quantileBuilder_ == null) {
          if (((bitField0_ & 0x00000004) == 0x00000004)) {
            quantile_ = java.util.Collections.unmodifiableList(quantile_);
            bitField0_ = (bitField0_ & ~0x00000004);
          }
          result.quantile_ = quantile_;
        } else {
          result.quantile_ = quantileBuilder_.build();
        }
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.prometheus.client.Metrics.Summary) {
          return mergeFrom((io.prometheus.client.Metrics.Summary)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.prometheus.client.Metrics.Summary other) {
        if (other == io.prometheus.client.Metrics.Summary.getDefaultInstance()) return this;
        if (other.hasSampleCount()) {
          setSampleCount(other.getSampleCount());
        }
        if (other.hasSampleSum()) {
          setSampleSum(other.getSampleSum());
        }
        if (quantileBuilder_ == null) {
          if (!other.quantile_.isEmpty()) {
            if (quantile_.isEmpty()) {
              quantile_ = other.quantile_;
              bitField0_ = (bitField0_ & ~0x00000004);
            } else {
              ensureQuantileIsMutable();
              quantile_.addAll(other.quantile_);
            }
            onChanged();
          }
        } else {
          if (!other.quantile_.isEmpty()) {
            if (quantileBuilder_.isEmpty()) {
              quantileBuilder_.dispose();
              quantileBuilder_ = null;
              quantile_ = other.quantile_;
              bitField0_ = (bitField0_ & ~0x00000004);
              quantileBuilder_ =
                      com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                              getQuantileFieldBuilder() : null;
            } else {
              quantileBuilder_.addAllMessages(other.quantile_);
            }
          }
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws java.io.IOException {
        io.prometheus.client.Metrics.Summary parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.prometheus.client.Metrics.Summary) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private long sampleCount_ ;
      /**
       * <code>optional uint64 sample_count = 1;</code>
       */
      public boolean hasSampleCount() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional uint64 sample_count = 1;</code>
       */
      public long getSampleCount() {
        return sampleCount_;
      }
      /**
       * <code>optional uint64 sample_count = 1;</code>
       */
      public Builder setSampleCount(long value) {
        bitField0_ |= 0x00000001;
        sampleCount_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional uint64 sample_count = 1;</code>
       */
      public Builder clearSampleCount() {
        bitField0_ = (bitField0_ & ~0x00000001);
        sampleCount_ = 0L;
        onChanged();
        return this;
      }

      private double sampleSum_ ;
      /**
       * <code>optional double sample_sum = 2;</code>
       */
      public boolean hasSampleSum() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional double sample_sum = 2;</code>
       */
      public double getSampleSum() {
        return sampleSum_;
      }
      /**
       * <code>optional double sample_sum = 2;</code>
       */
      public Builder setSampleSum(double value) {
        bitField0_ |= 0x00000002;
        sampleSum_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double sample_sum = 2;</code>
       */
      public Builder clearSampleSum() {
        bitField0_ = (bitField0_ & ~0x00000002);
        sampleSum_ = 0D;
        onChanged();
        return this;
      }

      private java.util.List<Quantile> quantile_ =
              java.util.Collections.emptyList();
      private void ensureQuantileIsMutable() {
        if (!((bitField0_ & 0x00000004) == 0x00000004)) {
          quantile_ = new java.util.ArrayList<Quantile>(quantile_);
          bitField0_ |= 0x00000004;
        }
      }

      private com.google.protobuf.RepeatedFieldBuilder<
              io.prometheus.client.Metrics.Quantile, io.prometheus.client.Metrics.Quantile.Builder, io.prometheus.client.Metrics.QuantileOrBuilder> quantileBuilder_;

      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public java.util.List<Quantile> getQuantileList() {
        if (quantileBuilder_ == null) {
          return java.util.Collections.unmodifiableList(quantile_);
        } else {
          return quantileBuilder_.getMessageList();
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public int getQuantileCount() {
        if (quantileBuilder_ == null) {
          return quantile_.size();
        } else {
          return quantileBuilder_.getCount();
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public io.prometheus.client.Metrics.Quantile getQuantile(int index) {
        if (quantileBuilder_ == null) {
          return quantile_.get(index);
        } else {
          return quantileBuilder_.getMessage(index);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public Builder setQuantile(
              int index, io.prometheus.client.Metrics.Quantile value) {
        if (quantileBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureQuantileIsMutable();
          quantile_.set(index, value);
          onChanged();
        } else {
          quantileBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public Builder setQuantile(
              int index, io.prometheus.client.Metrics.Quantile.Builder builderForValue) {
        if (quantileBuilder_ == null) {
          ensureQuantileIsMutable();
          quantile_.set(index, builderForValue.build());
          onChanged();
        } else {
          quantileBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public Builder addQuantile(io.prometheus.client.Metrics.Quantile value) {
        if (quantileBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureQuantileIsMutable();
          quantile_.add(value);
          onChanged();
        } else {
          quantileBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public Builder addQuantile(
              int index, io.prometheus.client.Metrics.Quantile value) {
        if (quantileBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureQuantileIsMutable();
          quantile_.add(index, value);
          onChanged();
        } else {
          quantileBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public Builder addQuantile(
              io.prometheus.client.Metrics.Quantile.Builder builderForValue) {
        if (quantileBuilder_ == null) {
          ensureQuantileIsMutable();
          quantile_.add(builderForValue.build());
          onChanged();
        } else {
          quantileBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public Builder addQuantile(
              int index, io.prometheus.client.Metrics.Quantile.Builder builderForValue) {
        if (quantileBuilder_ == null) {
          ensureQuantileIsMutable();
          quantile_.add(index, builderForValue.build());
          onChanged();
        } else {
          quantileBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public Builder addAllQuantile(
              Iterable<? extends Quantile> values) {
        if (quantileBuilder_ == null) {
          ensureQuantileIsMutable();
          com.google.protobuf.AbstractMessageLite.Builder.addAll(
                  values, quantile_);
          onChanged();
        } else {
          quantileBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public Builder clearQuantile() {
        if (quantileBuilder_ == null) {
          quantile_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000004);
          onChanged();
        } else {
          quantileBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public Builder removeQuantile(int index) {
        if (quantileBuilder_ == null) {
          ensureQuantileIsMutable();
          quantile_.remove(index);
          onChanged();
        } else {
          quantileBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public io.prometheus.client.Metrics.Quantile.Builder getQuantileBuilder(
              int index) {
        return getQuantileFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public io.prometheus.client.Metrics.QuantileOrBuilder getQuantileOrBuilder(
              int index) {
        if (quantileBuilder_ == null) {
          return quantile_.get(index);  } else {
          return quantileBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public java.util.List<? extends QuantileOrBuilder>
      getQuantileOrBuilderList() {
        if (quantileBuilder_ != null) {
          return quantileBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(quantile_);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public io.prometheus.client.Metrics.Quantile.Builder addQuantileBuilder() {
        return getQuantileFieldBuilder().addBuilder(
                io.prometheus.client.Metrics.Quantile.getDefaultInstance());
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public io.prometheus.client.Metrics.Quantile.Builder addQuantileBuilder(
              int index) {
        return getQuantileFieldBuilder().addBuilder(
                index, io.prometheus.client.Metrics.Quantile.getDefaultInstance());
      }
      /**
       * <code>repeated .io.prometheus.client.Quantile quantile = 3;</code>
       */
      public java.util.List<Quantile.Builder>
      getQuantileBuilderList() {
        return getQuantileFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilder<
              io.prometheus.client.Metrics.Quantile, io.prometheus.client.Metrics.Quantile.Builder, io.prometheus.client.Metrics.QuantileOrBuilder>
      getQuantileFieldBuilder() {
        if (quantileBuilder_ == null) {
          quantileBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
                  io.prometheus.client.Metrics.Quantile, io.prometheus.client.Metrics.Quantile.Builder, io.prometheus.client.Metrics.QuantileOrBuilder>(
                  quantile_,
                  ((bitField0_ & 0x00000004) == 0x00000004),
                  getParentForChildren(),
                  isClean());
          quantile_ = null;
        }
        return quantileBuilder_;
      }

      // @@protoc_insertion_point(builder_scope:io.prometheus.client.Summary)
    }

    static {
      defaultInstance = new Summary(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:io.prometheus.client.Summary)
  }

  public interface UntypedOrBuilder extends
          // @@protoc_insertion_point(interface_extends:io.prometheus.client.Untyped)
          com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional double value = 1;</code>
     */
    boolean hasValue();
    /**
     * <code>optional double value = 1;</code>
     */
    double getValue();
  }
  /**
   * Protobuf type {@code io.prometheus.client.Untyped}
   */
  public static final class Untyped extends
          com.google.protobuf.GeneratedMessage implements
          // @@protoc_insertion_point(message_implements:io.prometheus.client.Untyped)
          UntypedOrBuilder {
    // Use Untyped.newBuilder() to construct.
    private Untyped(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Untyped(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Untyped defaultInstance;
    public static Untyped getDefaultInstance() {
      return defaultInstance;
    }

    public Untyped getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Untyped(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
              com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                      extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 9: {
              bitField0_ |= 0x00000001;
              value_ = input.readDouble();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Untyped_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Untyped_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      io.prometheus.client.Metrics.Untyped.class, io.prometheus.client.Metrics.Untyped.Builder.class);
    }

    public static com.google.protobuf.Parser<Untyped> PARSER =
            new com.google.protobuf.AbstractParser<Untyped>() {
              public Untyped parsePartialFrom(
                      com.google.protobuf.CodedInputStream input,
                      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                      throws com.google.protobuf.InvalidProtocolBufferException {
                return new Untyped(input, extensionRegistry);
              }
            };

    @Override
    public com.google.protobuf.Parser<Untyped> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int VALUE_FIELD_NUMBER = 1;
    private double value_;
    /**
     * <code>optional double value = 1;</code>
     */
    public boolean hasValue() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional double value = 1;</code>
     */
    public double getValue() {
      return value_;
    }

    private void initFields() {
      value_ = 0D;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeDouble(1, value_);
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
                .computeDoubleSize(1, value_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
            throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static io.prometheus.client.Metrics.Untyped parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Untyped parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Untyped parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Untyped parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Untyped parseFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Untyped parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Untyped parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static io.prometheus.client.Metrics.Untyped parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Untyped parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Untyped parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(io.prometheus.client.Metrics.Untyped prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code io.prometheus.client.Untyped}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessage.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:io.prometheus.client.Untyped)
            io.prometheus.client.Metrics.UntypedOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Untyped_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Untyped_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        io.prometheus.client.Metrics.Untyped.class, io.prometheus.client.Metrics.Untyped.Builder.class);
      }

      // Construct using io.prometheus.client.Metrics.Untyped.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
              com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        value_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
      getDescriptorForType() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Untyped_descriptor;
      }

      public io.prometheus.client.Metrics.Untyped getDefaultInstanceForType() {
        return io.prometheus.client.Metrics.Untyped.getDefaultInstance();
      }

      public io.prometheus.client.Metrics.Untyped build() {
        io.prometheus.client.Metrics.Untyped result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public io.prometheus.client.Metrics.Untyped buildPartial() {
        io.prometheus.client.Metrics.Untyped result = new io.prometheus.client.Metrics.Untyped(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.value_ = value_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.prometheus.client.Metrics.Untyped) {
          return mergeFrom((io.prometheus.client.Metrics.Untyped)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.prometheus.client.Metrics.Untyped other) {
        if (other == io.prometheus.client.Metrics.Untyped.getDefaultInstance()) return this;
        if (other.hasValue()) {
          setValue(other.getValue());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws java.io.IOException {
        io.prometheus.client.Metrics.Untyped parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.prometheus.client.Metrics.Untyped) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private double value_ ;
      /**
       * <code>optional double value = 1;</code>
       */
      public boolean hasValue() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional double value = 1;</code>
       */
      public double getValue() {
        return value_;
      }
      /**
       * <code>optional double value = 1;</code>
       */
      public Builder setValue(double value) {
        bitField0_ |= 0x00000001;
        value_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double value = 1;</code>
       */
      public Builder clearValue() {
        bitField0_ = (bitField0_ & ~0x00000001);
        value_ = 0D;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:io.prometheus.client.Untyped)
    }

    static {
      defaultInstance = new Untyped(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:io.prometheus.client.Untyped)
  }

  public interface HistogramOrBuilder extends
          // @@protoc_insertion_point(interface_extends:io.prometheus.client.Histogram)
          com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional uint64 sample_count = 1;</code>
     */
    boolean hasSampleCount();
    /**
     * <code>optional uint64 sample_count = 1;</code>
     */
    long getSampleCount();

    /**
     * <code>optional double sample_sum = 2;</code>
     */
    boolean hasSampleSum();
    /**
     * <code>optional double sample_sum = 2;</code>
     */
    double getSampleSum();

    /**
     * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
     *
     * <pre>
     * Ordered in increasing order of upper_bound, +Inf bucket is optional.
     * </pre>
     */
    java.util.List<Bucket>
    getBucketList();
    /**
     * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
     *
     * <pre>
     * Ordered in increasing order of upper_bound, +Inf bucket is optional.
     * </pre>
     */
    io.prometheus.client.Metrics.Bucket getBucket(int index);
    /**
     * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
     *
     * <pre>
     * Ordered in increasing order of upper_bound, +Inf bucket is optional.
     * </pre>
     */
    int getBucketCount();
    /**
     * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
     *
     * <pre>
     * Ordered in increasing order of upper_bound, +Inf bucket is optional.
     * </pre>
     */
    java.util.List<? extends BucketOrBuilder>
    getBucketOrBuilderList();
    /**
     * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
     *
     * <pre>
     * Ordered in increasing order of upper_bound, +Inf bucket is optional.
     * </pre>
     */
    io.prometheus.client.Metrics.BucketOrBuilder getBucketOrBuilder(
            int index);
  }
  /**
   * Protobuf type {@code io.prometheus.client.Histogram}
   */
  public static final class Histogram extends
          com.google.protobuf.GeneratedMessage implements
          // @@protoc_insertion_point(message_implements:io.prometheus.client.Histogram)
          HistogramOrBuilder {
    // Use Histogram.newBuilder() to construct.
    private Histogram(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Histogram(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Histogram defaultInstance;
    public static Histogram getDefaultInstance() {
      return defaultInstance;
    }

    public Histogram getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Histogram(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
              com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                      extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              sampleCount_ = input.readUInt64();
              break;
            }
            case 17: {
              bitField0_ |= 0x00000002;
              sampleSum_ = input.readDouble();
              break;
            }
            case 26: {
              if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
                bucket_ = new java.util.ArrayList<Bucket>();
                mutable_bitField0_ |= 0x00000004;
              }
              bucket_.add(input.readMessage(io.prometheus.client.Metrics.Bucket.PARSER, extensionRegistry));
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
          bucket_ = java.util.Collections.unmodifiableList(bucket_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Histogram_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Histogram_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      io.prometheus.client.Metrics.Histogram.class, io.prometheus.client.Metrics.Histogram.Builder.class);
    }

    public static com.google.protobuf.Parser<Histogram> PARSER =
            new com.google.protobuf.AbstractParser<Histogram>() {
              public Histogram parsePartialFrom(
                      com.google.protobuf.CodedInputStream input,
                      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                      throws com.google.protobuf.InvalidProtocolBufferException {
                return new Histogram(input, extensionRegistry);
              }
            };

    @Override
    public com.google.protobuf.Parser<Histogram> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int SAMPLE_COUNT_FIELD_NUMBER = 1;
    private long sampleCount_;
    /**
     * <code>optional uint64 sample_count = 1;</code>
     */
    public boolean hasSampleCount() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional uint64 sample_count = 1;</code>
     */
    public long getSampleCount() {
      return sampleCount_;
    }

    public static final int SAMPLE_SUM_FIELD_NUMBER = 2;
    private double sampleSum_;
    /**
     * <code>optional double sample_sum = 2;</code>
     */
    public boolean hasSampleSum() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional double sample_sum = 2;</code>
     */
    public double getSampleSum() {
      return sampleSum_;
    }

    public static final int BUCKET_FIELD_NUMBER = 3;
    private java.util.List<Bucket> bucket_;
    /**
     * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
     *
     * <pre>
     * Ordered in increasing order of upper_bound, +Inf bucket is optional.
     * </pre>
     */
    public java.util.List<Bucket> getBucketList() {
      return bucket_;
    }
    /**
     * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
     *
     * <pre>
     * Ordered in increasing order of upper_bound, +Inf bucket is optional.
     * </pre>
     */
    public java.util.List<? extends BucketOrBuilder>
    getBucketOrBuilderList() {
      return bucket_;
    }
    /**
     * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
     *
     * <pre>
     * Ordered in increasing order of upper_bound, +Inf bucket is optional.
     * </pre>
     */
    public int getBucketCount() {
      return bucket_.size();
    }
    /**
     * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
     *
     * <pre>
     * Ordered in increasing order of upper_bound, +Inf bucket is optional.
     * </pre>
     */
    public io.prometheus.client.Metrics.Bucket getBucket(int index) {
      return bucket_.get(index);
    }
    /**
     * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
     *
     * <pre>
     * Ordered in increasing order of upper_bound, +Inf bucket is optional.
     * </pre>
     */
    public io.prometheus.client.Metrics.BucketOrBuilder getBucketOrBuilder(
            int index) {
      return bucket_.get(index);
    }

    private void initFields() {
      sampleCount_ = 0L;
      sampleSum_ = 0D;
      bucket_ = java.util.Collections.emptyList();
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeUInt64(1, sampleCount_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeDouble(2, sampleSum_);
      }
      for (int i = 0; i < bucket_.size(); i++) {
        output.writeMessage(3, bucket_.get(i));
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
                .computeUInt64Size(1, sampleCount_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
                .computeDoubleSize(2, sampleSum_);
      }
      for (int i = 0; i < bucket_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(3, bucket_.get(i));
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
            throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static io.prometheus.client.Metrics.Histogram parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Histogram parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Histogram parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Histogram parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Histogram parseFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Histogram parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Histogram parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static io.prometheus.client.Metrics.Histogram parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Histogram parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Histogram parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(io.prometheus.client.Metrics.Histogram prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code io.prometheus.client.Histogram}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessage.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:io.prometheus.client.Histogram)
            io.prometheus.client.Metrics.HistogramOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Histogram_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Histogram_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        io.prometheus.client.Metrics.Histogram.class, io.prometheus.client.Metrics.Histogram.Builder.class);
      }

      // Construct using io.prometheus.client.Metrics.Histogram.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
              com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          getBucketFieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        sampleCount_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000001);
        sampleSum_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000002);
        if (bucketBuilder_ == null) {
          bucket_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000004);
        } else {
          bucketBuilder_.clear();
        }
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
      getDescriptorForType() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Histogram_descriptor;
      }

      public io.prometheus.client.Metrics.Histogram getDefaultInstanceForType() {
        return io.prometheus.client.Metrics.Histogram.getDefaultInstance();
      }

      public io.prometheus.client.Metrics.Histogram build() {
        io.prometheus.client.Metrics.Histogram result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public io.prometheus.client.Metrics.Histogram buildPartial() {
        io.prometheus.client.Metrics.Histogram result = new io.prometheus.client.Metrics.Histogram(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.sampleCount_ = sampleCount_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.sampleSum_ = sampleSum_;
        if (bucketBuilder_ == null) {
          if (((bitField0_ & 0x00000004) == 0x00000004)) {
            bucket_ = java.util.Collections.unmodifiableList(bucket_);
            bitField0_ = (bitField0_ & ~0x00000004);
          }
          result.bucket_ = bucket_;
        } else {
          result.bucket_ = bucketBuilder_.build();
        }
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.prometheus.client.Metrics.Histogram) {
          return mergeFrom((io.prometheus.client.Metrics.Histogram)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.prometheus.client.Metrics.Histogram other) {
        if (other == io.prometheus.client.Metrics.Histogram.getDefaultInstance()) return this;
        if (other.hasSampleCount()) {
          setSampleCount(other.getSampleCount());
        }
        if (other.hasSampleSum()) {
          setSampleSum(other.getSampleSum());
        }
        if (bucketBuilder_ == null) {
          if (!other.bucket_.isEmpty()) {
            if (bucket_.isEmpty()) {
              bucket_ = other.bucket_;
              bitField0_ = (bitField0_ & ~0x00000004);
            } else {
              ensureBucketIsMutable();
              bucket_.addAll(other.bucket_);
            }
            onChanged();
          }
        } else {
          if (!other.bucket_.isEmpty()) {
            if (bucketBuilder_.isEmpty()) {
              bucketBuilder_.dispose();
              bucketBuilder_ = null;
              bucket_ = other.bucket_;
              bitField0_ = (bitField0_ & ~0x00000004);
              bucketBuilder_ =
                      com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                              getBucketFieldBuilder() : null;
            } else {
              bucketBuilder_.addAllMessages(other.bucket_);
            }
          }
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws java.io.IOException {
        io.prometheus.client.Metrics.Histogram parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.prometheus.client.Metrics.Histogram) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private long sampleCount_ ;
      /**
       * <code>optional uint64 sample_count = 1;</code>
       */
      public boolean hasSampleCount() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional uint64 sample_count = 1;</code>
       */
      public long getSampleCount() {
        return sampleCount_;
      }
      /**
       * <code>optional uint64 sample_count = 1;</code>
       */
      public Builder setSampleCount(long value) {
        bitField0_ |= 0x00000001;
        sampleCount_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional uint64 sample_count = 1;</code>
       */
      public Builder clearSampleCount() {
        bitField0_ = (bitField0_ & ~0x00000001);
        sampleCount_ = 0L;
        onChanged();
        return this;
      }

      private double sampleSum_ ;
      /**
       * <code>optional double sample_sum = 2;</code>
       */
      public boolean hasSampleSum() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional double sample_sum = 2;</code>
       */
      public double getSampleSum() {
        return sampleSum_;
      }
      /**
       * <code>optional double sample_sum = 2;</code>
       */
      public Builder setSampleSum(double value) {
        bitField0_ |= 0x00000002;
        sampleSum_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double sample_sum = 2;</code>
       */
      public Builder clearSampleSum() {
        bitField0_ = (bitField0_ & ~0x00000002);
        sampleSum_ = 0D;
        onChanged();
        return this;
      }

      private java.util.List<Bucket> bucket_ =
              java.util.Collections.emptyList();
      private void ensureBucketIsMutable() {
        if (!((bitField0_ & 0x00000004) == 0x00000004)) {
          bucket_ = new java.util.ArrayList<Bucket>(bucket_);
          bitField0_ |= 0x00000004;
        }
      }

      private com.google.protobuf.RepeatedFieldBuilder<
              io.prometheus.client.Metrics.Bucket, io.prometheus.client.Metrics.Bucket.Builder, io.prometheus.client.Metrics.BucketOrBuilder> bucketBuilder_;

      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public java.util.List<Bucket> getBucketList() {
        if (bucketBuilder_ == null) {
          return java.util.Collections.unmodifiableList(bucket_);
        } else {
          return bucketBuilder_.getMessageList();
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public int getBucketCount() {
        if (bucketBuilder_ == null) {
          return bucket_.size();
        } else {
          return bucketBuilder_.getCount();
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public io.prometheus.client.Metrics.Bucket getBucket(int index) {
        if (bucketBuilder_ == null) {
          return bucket_.get(index);
        } else {
          return bucketBuilder_.getMessage(index);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public Builder setBucket(
              int index, io.prometheus.client.Metrics.Bucket value) {
        if (bucketBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureBucketIsMutable();
          bucket_.set(index, value);
          onChanged();
        } else {
          bucketBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public Builder setBucket(
              int index, io.prometheus.client.Metrics.Bucket.Builder builderForValue) {
        if (bucketBuilder_ == null) {
          ensureBucketIsMutable();
          bucket_.set(index, builderForValue.build());
          onChanged();
        } else {
          bucketBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public Builder addBucket(io.prometheus.client.Metrics.Bucket value) {
        if (bucketBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureBucketIsMutable();
          bucket_.add(value);
          onChanged();
        } else {
          bucketBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public Builder addBucket(
              int index, io.prometheus.client.Metrics.Bucket value) {
        if (bucketBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureBucketIsMutable();
          bucket_.add(index, value);
          onChanged();
        } else {
          bucketBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public Builder addBucket(
              io.prometheus.client.Metrics.Bucket.Builder builderForValue) {
        if (bucketBuilder_ == null) {
          ensureBucketIsMutable();
          bucket_.add(builderForValue.build());
          onChanged();
        } else {
          bucketBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public Builder addBucket(
              int index, io.prometheus.client.Metrics.Bucket.Builder builderForValue) {
        if (bucketBuilder_ == null) {
          ensureBucketIsMutable();
          bucket_.add(index, builderForValue.build());
          onChanged();
        } else {
          bucketBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public Builder addAllBucket(
              Iterable<? extends Bucket> values) {
        if (bucketBuilder_ == null) {
          ensureBucketIsMutable();
          com.google.protobuf.AbstractMessageLite.Builder.addAll(
                  values, bucket_);
          onChanged();
        } else {
          bucketBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public Builder clearBucket() {
        if (bucketBuilder_ == null) {
          bucket_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000004);
          onChanged();
        } else {
          bucketBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public Builder removeBucket(int index) {
        if (bucketBuilder_ == null) {
          ensureBucketIsMutable();
          bucket_.remove(index);
          onChanged();
        } else {
          bucketBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public io.prometheus.client.Metrics.Bucket.Builder getBucketBuilder(
              int index) {
        return getBucketFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public io.prometheus.client.Metrics.BucketOrBuilder getBucketOrBuilder(
              int index) {
        if (bucketBuilder_ == null) {
          return bucket_.get(index);  } else {
          return bucketBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public java.util.List<? extends BucketOrBuilder>
      getBucketOrBuilderList() {
        if (bucketBuilder_ != null) {
          return bucketBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(bucket_);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public io.prometheus.client.Metrics.Bucket.Builder addBucketBuilder() {
        return getBucketFieldBuilder().addBuilder(
                io.prometheus.client.Metrics.Bucket.getDefaultInstance());
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public io.prometheus.client.Metrics.Bucket.Builder addBucketBuilder(
              int index) {
        return getBucketFieldBuilder().addBuilder(
                index, io.prometheus.client.Metrics.Bucket.getDefaultInstance());
      }
      /**
       * <code>repeated .io.prometheus.client.Bucket bucket = 3;</code>
       *
       * <pre>
       * Ordered in increasing order of upper_bound, +Inf bucket is optional.
       * </pre>
       */
      public java.util.List<Bucket.Builder>
      getBucketBuilderList() {
        return getBucketFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilder<
              io.prometheus.client.Metrics.Bucket, io.prometheus.client.Metrics.Bucket.Builder, io.prometheus.client.Metrics.BucketOrBuilder>
      getBucketFieldBuilder() {
        if (bucketBuilder_ == null) {
          bucketBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
                  io.prometheus.client.Metrics.Bucket, io.prometheus.client.Metrics.Bucket.Builder, io.prometheus.client.Metrics.BucketOrBuilder>(
                  bucket_,
                  ((bitField0_ & 0x00000004) == 0x00000004),
                  getParentForChildren(),
                  isClean());
          bucket_ = null;
        }
        return bucketBuilder_;
      }

      // @@protoc_insertion_point(builder_scope:io.prometheus.client.Histogram)
    }

    static {
      defaultInstance = new Histogram(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:io.prometheus.client.Histogram)
  }

  public interface BucketOrBuilder extends
          // @@protoc_insertion_point(interface_extends:io.prometheus.client.Bucket)
          com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional uint64 cumulative_count = 1;</code>
     *
     * <pre>
     * Cumulative in increasing order.
     * </pre>
     */
    boolean hasCumulativeCount();
    /**
     * <code>optional uint64 cumulative_count = 1;</code>
     *
     * <pre>
     * Cumulative in increasing order.
     * </pre>
     */
    long getCumulativeCount();

    /**
     * <code>optional double upper_bound = 2;</code>
     *
     * <pre>
     * Inclusive.
     * </pre>
     */
    boolean hasUpperBound();
    /**
     * <code>optional double upper_bound = 2;</code>
     *
     * <pre>
     * Inclusive.
     * </pre>
     */
    double getUpperBound();
  }
  /**
   * Protobuf type {@code io.prometheus.client.Bucket}
   */
  public static final class Bucket extends
          com.google.protobuf.GeneratedMessage implements
          // @@protoc_insertion_point(message_implements:io.prometheus.client.Bucket)
          BucketOrBuilder {
    // Use Bucket.newBuilder() to construct.
    private Bucket(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Bucket(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Bucket defaultInstance;
    public static Bucket getDefaultInstance() {
      return defaultInstance;
    }

    public Bucket getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Bucket(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
              com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                      extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              cumulativeCount_ = input.readUInt64();
              break;
            }
            case 17: {
              bitField0_ |= 0x00000002;
              upperBound_ = input.readDouble();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Bucket_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Bucket_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      io.prometheus.client.Metrics.Bucket.class, io.prometheus.client.Metrics.Bucket.Builder.class);
    }

    public static com.google.protobuf.Parser<Bucket> PARSER =
            new com.google.protobuf.AbstractParser<Bucket>() {
              public Bucket parsePartialFrom(
                      com.google.protobuf.CodedInputStream input,
                      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                      throws com.google.protobuf.InvalidProtocolBufferException {
                return new Bucket(input, extensionRegistry);
              }
            };

    @Override
    public com.google.protobuf.Parser<Bucket> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int CUMULATIVE_COUNT_FIELD_NUMBER = 1;
    private long cumulativeCount_;
    /**
     * <code>optional uint64 cumulative_count = 1;</code>
     *
     * <pre>
     * Cumulative in increasing order.
     * </pre>
     */
    public boolean hasCumulativeCount() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional uint64 cumulative_count = 1;</code>
     *
     * <pre>
     * Cumulative in increasing order.
     * </pre>
     */
    public long getCumulativeCount() {
      return cumulativeCount_;
    }

    public static final int UPPER_BOUND_FIELD_NUMBER = 2;
    private double upperBound_;
    /**
     * <code>optional double upper_bound = 2;</code>
     *
     * <pre>
     * Inclusive.
     * </pre>
     */
    public boolean hasUpperBound() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional double upper_bound = 2;</code>
     *
     * <pre>
     * Inclusive.
     * </pre>
     */
    public double getUpperBound() {
      return upperBound_;
    }

    private void initFields() {
      cumulativeCount_ = 0L;
      upperBound_ = 0D;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeUInt64(1, cumulativeCount_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeDouble(2, upperBound_);
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
                .computeUInt64Size(1, cumulativeCount_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
                .computeDoubleSize(2, upperBound_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
            throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static io.prometheus.client.Metrics.Bucket parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Bucket parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Bucket parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Bucket parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Bucket parseFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Bucket parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Bucket parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static io.prometheus.client.Metrics.Bucket parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Bucket parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Bucket parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(io.prometheus.client.Metrics.Bucket prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code io.prometheus.client.Bucket}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessage.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:io.prometheus.client.Bucket)
            io.prometheus.client.Metrics.BucketOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Bucket_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Bucket_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        io.prometheus.client.Metrics.Bucket.class, io.prometheus.client.Metrics.Bucket.Builder.class);
      }

      // Construct using io.prometheus.client.Metrics.Bucket.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
              com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        cumulativeCount_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000001);
        upperBound_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
      getDescriptorForType() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Bucket_descriptor;
      }

      public io.prometheus.client.Metrics.Bucket getDefaultInstanceForType() {
        return io.prometheus.client.Metrics.Bucket.getDefaultInstance();
      }

      public io.prometheus.client.Metrics.Bucket build() {
        io.prometheus.client.Metrics.Bucket result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public io.prometheus.client.Metrics.Bucket buildPartial() {
        io.prometheus.client.Metrics.Bucket result = new io.prometheus.client.Metrics.Bucket(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.cumulativeCount_ = cumulativeCount_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.upperBound_ = upperBound_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.prometheus.client.Metrics.Bucket) {
          return mergeFrom((io.prometheus.client.Metrics.Bucket)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.prometheus.client.Metrics.Bucket other) {
        if (other == io.prometheus.client.Metrics.Bucket.getDefaultInstance()) return this;
        if (other.hasCumulativeCount()) {
          setCumulativeCount(other.getCumulativeCount());
        }
        if (other.hasUpperBound()) {
          setUpperBound(other.getUpperBound());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws java.io.IOException {
        io.prometheus.client.Metrics.Bucket parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.prometheus.client.Metrics.Bucket) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private long cumulativeCount_ ;
      /**
       * <code>optional uint64 cumulative_count = 1;</code>
       *
       * <pre>
       * Cumulative in increasing order.
       * </pre>
       */
      public boolean hasCumulativeCount() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional uint64 cumulative_count = 1;</code>
       *
       * <pre>
       * Cumulative in increasing order.
       * </pre>
       */
      public long getCumulativeCount() {
        return cumulativeCount_;
      }
      /**
       * <code>optional uint64 cumulative_count = 1;</code>
       *
       * <pre>
       * Cumulative in increasing order.
       * </pre>
       */
      public Builder setCumulativeCount(long value) {
        bitField0_ |= 0x00000001;
        cumulativeCount_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional uint64 cumulative_count = 1;</code>
       *
       * <pre>
       * Cumulative in increasing order.
       * </pre>
       */
      public Builder clearCumulativeCount() {
        bitField0_ = (bitField0_ & ~0x00000001);
        cumulativeCount_ = 0L;
        onChanged();
        return this;
      }

      private double upperBound_ ;
      /**
       * <code>optional double upper_bound = 2;</code>
       *
       * <pre>
       * Inclusive.
       * </pre>
       */
      public boolean hasUpperBound() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional double upper_bound = 2;</code>
       *
       * <pre>
       * Inclusive.
       * </pre>
       */
      public double getUpperBound() {
        return upperBound_;
      }
      /**
       * <code>optional double upper_bound = 2;</code>
       *
       * <pre>
       * Inclusive.
       * </pre>
       */
      public Builder setUpperBound(double value) {
        bitField0_ |= 0x00000002;
        upperBound_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double upper_bound = 2;</code>
       *
       * <pre>
       * Inclusive.
       * </pre>
       */
      public Builder clearUpperBound() {
        bitField0_ = (bitField0_ & ~0x00000002);
        upperBound_ = 0D;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:io.prometheus.client.Bucket)
    }

    static {
      defaultInstance = new Bucket(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:io.prometheus.client.Bucket)
  }

  public interface MetricOrBuilder extends
          // @@protoc_insertion_point(interface_extends:io.prometheus.client.Metric)
          com.google.protobuf.MessageOrBuilder {

    /**
     * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
     */
    java.util.List<LabelPair>
    getLabelList();
    /**
     * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
     */
    io.prometheus.client.Metrics.LabelPair getLabel(int index);
    /**
     * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
     */
    int getLabelCount();
    /**
     * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
     */
    java.util.List<? extends LabelPairOrBuilder>
    getLabelOrBuilderList();
    /**
     * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
     */
    io.prometheus.client.Metrics.LabelPairOrBuilder getLabelOrBuilder(
            int index);

    /**
     * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
     */
    boolean hasGauge();
    /**
     * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
     */
    io.prometheus.client.Metrics.Gauge getGauge();
    /**
     * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
     */
    io.prometheus.client.Metrics.GaugeOrBuilder getGaugeOrBuilder();

    /**
     * <code>optional .io.prometheus.client.Counter counter = 3;</code>
     */
    boolean hasCounter();
    /**
     * <code>optional .io.prometheus.client.Counter counter = 3;</code>
     */
    io.prometheus.client.Metrics.Counter getCounter();
    /**
     * <code>optional .io.prometheus.client.Counter counter = 3;</code>
     */
    io.prometheus.client.Metrics.CounterOrBuilder getCounterOrBuilder();

    /**
     * <code>optional .io.prometheus.client.Summary summary = 4;</code>
     */
    boolean hasSummary();
    /**
     * <code>optional .io.prometheus.client.Summary summary = 4;</code>
     */
    io.prometheus.client.Metrics.Summary getSummary();
    /**
     * <code>optional .io.prometheus.client.Summary summary = 4;</code>
     */
    io.prometheus.client.Metrics.SummaryOrBuilder getSummaryOrBuilder();

    /**
     * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
     */
    boolean hasUntyped();
    /**
     * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
     */
    io.prometheus.client.Metrics.Untyped getUntyped();
    /**
     * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
     */
    io.prometheus.client.Metrics.UntypedOrBuilder getUntypedOrBuilder();

    /**
     * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
     */
    boolean hasHistogram();
    /**
     * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
     */
    io.prometheus.client.Metrics.Histogram getHistogram();
    /**
     * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
     */
    io.prometheus.client.Metrics.HistogramOrBuilder getHistogramOrBuilder();

    /**
     * <code>optional int64 timestamp_ms = 6;</code>
     */
    boolean hasTimestampMs();
    /**
     * <code>optional int64 timestamp_ms = 6;</code>
     */
    long getTimestampMs();
  }
  /**
   * Protobuf type {@code io.prometheus.client.Metric}
   */
  public static final class Metric extends
          com.google.protobuf.GeneratedMessage implements
          // @@protoc_insertion_point(message_implements:io.prometheus.client.Metric)
          MetricOrBuilder {
    // Use Metric.newBuilder() to construct.
    private Metric(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Metric(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Metric defaultInstance;
    public static Metric getDefaultInstance() {
      return defaultInstance;
    }

    public Metric getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Metric(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
              com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                      extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                label_ = new java.util.ArrayList<LabelPair>();
                mutable_bitField0_ |= 0x00000001;
              }
              label_.add(input.readMessage(io.prometheus.client.Metrics.LabelPair.PARSER, extensionRegistry));
              break;
            }
            case 18: {
              io.prometheus.client.Metrics.Gauge.Builder subBuilder = null;
              if (((bitField0_ & 0x00000001) == 0x00000001)) {
                subBuilder = gauge_.toBuilder();
              }
              gauge_ = input.readMessage(io.prometheus.client.Metrics.Gauge.PARSER, extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(gauge_);
                gauge_ = subBuilder.buildPartial();
              }
              bitField0_ |= 0x00000001;
              break;
            }
            case 26: {
              io.prometheus.client.Metrics.Counter.Builder subBuilder = null;
              if (((bitField0_ & 0x00000002) == 0x00000002)) {
                subBuilder = counter_.toBuilder();
              }
              counter_ = input.readMessage(io.prometheus.client.Metrics.Counter.PARSER, extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(counter_);
                counter_ = subBuilder.buildPartial();
              }
              bitField0_ |= 0x00000002;
              break;
            }
            case 34: {
              io.prometheus.client.Metrics.Summary.Builder subBuilder = null;
              if (((bitField0_ & 0x00000004) == 0x00000004)) {
                subBuilder = summary_.toBuilder();
              }
              summary_ = input.readMessage(io.prometheus.client.Metrics.Summary.PARSER, extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(summary_);
                summary_ = subBuilder.buildPartial();
              }
              bitField0_ |= 0x00000004;
              break;
            }
            case 42: {
              io.prometheus.client.Metrics.Untyped.Builder subBuilder = null;
              if (((bitField0_ & 0x00000008) == 0x00000008)) {
                subBuilder = untyped_.toBuilder();
              }
              untyped_ = input.readMessage(io.prometheus.client.Metrics.Untyped.PARSER, extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(untyped_);
                untyped_ = subBuilder.buildPartial();
              }
              bitField0_ |= 0x00000008;
              break;
            }
            case 48: {
              bitField0_ |= 0x00000020;
              timestampMs_ = input.readInt64();
              break;
            }
            case 58: {
              io.prometheus.client.Metrics.Histogram.Builder subBuilder = null;
              if (((bitField0_ & 0x00000010) == 0x00000010)) {
                subBuilder = histogram_.toBuilder();
              }
              histogram_ = input.readMessage(io.prometheus.client.Metrics.Histogram.PARSER, extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(histogram_);
                histogram_ = subBuilder.buildPartial();
              }
              bitField0_ |= 0x00000010;
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
          label_ = java.util.Collections.unmodifiableList(label_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Metric_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Metric_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      io.prometheus.client.Metrics.Metric.class, io.prometheus.client.Metrics.Metric.Builder.class);
    }

    public static com.google.protobuf.Parser<Metric> PARSER =
            new com.google.protobuf.AbstractParser<Metric>() {
              public Metric parsePartialFrom(
                      com.google.protobuf.CodedInputStream input,
                      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                      throws com.google.protobuf.InvalidProtocolBufferException {
                return new Metric(input, extensionRegistry);
              }
            };

    @Override
    public com.google.protobuf.Parser<Metric> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int LABEL_FIELD_NUMBER = 1;
    private java.util.List<LabelPair> label_;
    /**
     * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
     */
    public java.util.List<LabelPair> getLabelList() {
      return label_;
    }
    /**
     * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
     */
    public java.util.List<? extends LabelPairOrBuilder>
    getLabelOrBuilderList() {
      return label_;
    }
    /**
     * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
     */
    public int getLabelCount() {
      return label_.size();
    }
    /**
     * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
     */
    public io.prometheus.client.Metrics.LabelPair getLabel(int index) {
      return label_.get(index);
    }
    /**
     * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
     */
    public io.prometheus.client.Metrics.LabelPairOrBuilder getLabelOrBuilder(
            int index) {
      return label_.get(index);
    }

    public static final int GAUGE_FIELD_NUMBER = 2;
    private io.prometheus.client.Metrics.Gauge gauge_;
    /**
     * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
     */
    public boolean hasGauge() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
     */
    public io.prometheus.client.Metrics.Gauge getGauge() {
      return gauge_;
    }
    /**
     * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
     */
    public io.prometheus.client.Metrics.GaugeOrBuilder getGaugeOrBuilder() {
      return gauge_;
    }

    public static final int COUNTER_FIELD_NUMBER = 3;
    private io.prometheus.client.Metrics.Counter counter_;
    /**
     * <code>optional .io.prometheus.client.Counter counter = 3;</code>
     */
    public boolean hasCounter() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional .io.prometheus.client.Counter counter = 3;</code>
     */
    public io.prometheus.client.Metrics.Counter getCounter() {
      return counter_;
    }
    /**
     * <code>optional .io.prometheus.client.Counter counter = 3;</code>
     */
    public io.prometheus.client.Metrics.CounterOrBuilder getCounterOrBuilder() {
      return counter_;
    }

    public static final int SUMMARY_FIELD_NUMBER = 4;
    private io.prometheus.client.Metrics.Summary summary_;
    /**
     * <code>optional .io.prometheus.client.Summary summary = 4;</code>
     */
    public boolean hasSummary() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>optional .io.prometheus.client.Summary summary = 4;</code>
     */
    public io.prometheus.client.Metrics.Summary getSummary() {
      return summary_;
    }
    /**
     * <code>optional .io.prometheus.client.Summary summary = 4;</code>
     */
    public io.prometheus.client.Metrics.SummaryOrBuilder getSummaryOrBuilder() {
      return summary_;
    }

    public static final int UNTYPED_FIELD_NUMBER = 5;
    private io.prometheus.client.Metrics.Untyped untyped_;
    /**
     * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
     */
    public boolean hasUntyped() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    /**
     * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
     */
    public io.prometheus.client.Metrics.Untyped getUntyped() {
      return untyped_;
    }
    /**
     * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
     */
    public io.prometheus.client.Metrics.UntypedOrBuilder getUntypedOrBuilder() {
      return untyped_;
    }

    public static final int HISTOGRAM_FIELD_NUMBER = 7;
    private io.prometheus.client.Metrics.Histogram histogram_;
    /**
     * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
     */
    public boolean hasHistogram() {
      return ((bitField0_ & 0x00000010) == 0x00000010);
    }
    /**
     * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
     */
    public io.prometheus.client.Metrics.Histogram getHistogram() {
      return histogram_;
    }
    /**
     * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
     */
    public io.prometheus.client.Metrics.HistogramOrBuilder getHistogramOrBuilder() {
      return histogram_;
    }

    public static final int TIMESTAMP_MS_FIELD_NUMBER = 6;
    private long timestampMs_;
    /**
     * <code>optional int64 timestamp_ms = 6;</code>
     */
    public boolean hasTimestampMs() {
      return ((bitField0_ & 0x00000020) == 0x00000020);
    }
    /**
     * <code>optional int64 timestamp_ms = 6;</code>
     */
    public long getTimestampMs() {
      return timestampMs_;
    }

    private void initFields() {
      label_ = java.util.Collections.emptyList();
      gauge_ = io.prometheus.client.Metrics.Gauge.getDefaultInstance();
      counter_ = io.prometheus.client.Metrics.Counter.getDefaultInstance();
      summary_ = io.prometheus.client.Metrics.Summary.getDefaultInstance();
      untyped_ = io.prometheus.client.Metrics.Untyped.getDefaultInstance();
      histogram_ = io.prometheus.client.Metrics.Histogram.getDefaultInstance();
      timestampMs_ = 0L;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
      getSerializedSize();
      for (int i = 0; i < label_.size(); i++) {
        output.writeMessage(1, label_.get(i));
      }
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeMessage(2, gauge_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeMessage(3, counter_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeMessage(4, summary_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeMessage(5, untyped_);
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        output.writeInt64(6, timestampMs_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        output.writeMessage(7, histogram_);
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      for (int i = 0; i < label_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(1, label_.get(i));
      }
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(2, gauge_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(3, counter_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(4, summary_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(5, untyped_);
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        size += com.google.protobuf.CodedOutputStream
                .computeInt64Size(6, timestampMs_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(7, histogram_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
            throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static io.prometheus.client.Metrics.Metric parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Metric parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Metric parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.Metric parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Metric parseFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Metric parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Metric parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static io.prometheus.client.Metrics.Metric parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.Metric parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.Metric parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(io.prometheus.client.Metrics.Metric prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code io.prometheus.client.Metric}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessage.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:io.prometheus.client.Metric)
            io.prometheus.client.Metrics.MetricOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Metric_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Metric_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        io.prometheus.client.Metrics.Metric.class, io.prometheus.client.Metrics.Metric.Builder.class);
      }

      // Construct using io.prometheus.client.Metrics.Metric.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
              com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          getLabelFieldBuilder();
          getGaugeFieldBuilder();
          getCounterFieldBuilder();
          getSummaryFieldBuilder();
          getUntypedFieldBuilder();
          getHistogramFieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        if (labelBuilder_ == null) {
          label_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          labelBuilder_.clear();
        }
        if (gaugeBuilder_ == null) {
          gauge_ = io.prometheus.client.Metrics.Gauge.getDefaultInstance();
        } else {
          gaugeBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000002);
        if (counterBuilder_ == null) {
          counter_ = io.prometheus.client.Metrics.Counter.getDefaultInstance();
        } else {
          counterBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000004);
        if (summaryBuilder_ == null) {
          summary_ = io.prometheus.client.Metrics.Summary.getDefaultInstance();
        } else {
          summaryBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000008);
        if (untypedBuilder_ == null) {
          untyped_ = io.prometheus.client.Metrics.Untyped.getDefaultInstance();
        } else {
          untypedBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000010);
        if (histogramBuilder_ == null) {
          histogram_ = io.prometheus.client.Metrics.Histogram.getDefaultInstance();
        } else {
          histogramBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000020);
        timestampMs_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000040);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
      getDescriptorForType() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_Metric_descriptor;
      }

      public io.prometheus.client.Metrics.Metric getDefaultInstanceForType() {
        return io.prometheus.client.Metrics.Metric.getDefaultInstance();
      }

      public io.prometheus.client.Metrics.Metric build() {
        io.prometheus.client.Metrics.Metric result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public io.prometheus.client.Metrics.Metric buildPartial() {
        io.prometheus.client.Metrics.Metric result = new io.prometheus.client.Metrics.Metric(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (labelBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001)) {
            label_ = java.util.Collections.unmodifiableList(label_);
            bitField0_ = (bitField0_ & ~0x00000001);
          }
          result.label_ = label_;
        } else {
          result.label_ = labelBuilder_.build();
        }
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000001;
        }
        if (gaugeBuilder_ == null) {
          result.gauge_ = gauge_;
        } else {
          result.gauge_ = gaugeBuilder_.build();
        }
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000002;
        }
        if (counterBuilder_ == null) {
          result.counter_ = counter_;
        } else {
          result.counter_ = counterBuilder_.build();
        }
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000004;
        }
        if (summaryBuilder_ == null) {
          result.summary_ = summary_;
        } else {
          result.summary_ = summaryBuilder_.build();
        }
        if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
          to_bitField0_ |= 0x00000008;
        }
        if (untypedBuilder_ == null) {
          result.untyped_ = untyped_;
        } else {
          result.untyped_ = untypedBuilder_.build();
        }
        if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
          to_bitField0_ |= 0x00000010;
        }
        if (histogramBuilder_ == null) {
          result.histogram_ = histogram_;
        } else {
          result.histogram_ = histogramBuilder_.build();
        }
        if (((from_bitField0_ & 0x00000040) == 0x00000040)) {
          to_bitField0_ |= 0x00000020;
        }
        result.timestampMs_ = timestampMs_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.prometheus.client.Metrics.Metric) {
          return mergeFrom((io.prometheus.client.Metrics.Metric)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.prometheus.client.Metrics.Metric other) {
        if (other == io.prometheus.client.Metrics.Metric.getDefaultInstance()) return this;
        if (labelBuilder_ == null) {
          if (!other.label_.isEmpty()) {
            if (label_.isEmpty()) {
              label_ = other.label_;
              bitField0_ = (bitField0_ & ~0x00000001);
            } else {
              ensureLabelIsMutable();
              label_.addAll(other.label_);
            }
            onChanged();
          }
        } else {
          if (!other.label_.isEmpty()) {
            if (labelBuilder_.isEmpty()) {
              labelBuilder_.dispose();
              labelBuilder_ = null;
              label_ = other.label_;
              bitField0_ = (bitField0_ & ~0x00000001);
              labelBuilder_ =
                      com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                              getLabelFieldBuilder() : null;
            } else {
              labelBuilder_.addAllMessages(other.label_);
            }
          }
        }
        if (other.hasGauge()) {
          mergeGauge(other.getGauge());
        }
        if (other.hasCounter()) {
          mergeCounter(other.getCounter());
        }
        if (other.hasSummary()) {
          mergeSummary(other.getSummary());
        }
        if (other.hasUntyped()) {
          mergeUntyped(other.getUntyped());
        }
        if (other.hasHistogram()) {
          mergeHistogram(other.getHistogram());
        }
        if (other.hasTimestampMs()) {
          setTimestampMs(other.getTimestampMs());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws java.io.IOException {
        io.prometheus.client.Metrics.Metric parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.prometheus.client.Metrics.Metric) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private java.util.List<LabelPair> label_ =
              java.util.Collections.emptyList();
      private void ensureLabelIsMutable() {
        if (!((bitField0_ & 0x00000001) == 0x00000001)) {
          label_ = new java.util.ArrayList<LabelPair>(label_);
          bitField0_ |= 0x00000001;
        }
      }

      private com.google.protobuf.RepeatedFieldBuilder<
              io.prometheus.client.Metrics.LabelPair, io.prometheus.client.Metrics.LabelPair.Builder, io.prometheus.client.Metrics.LabelPairOrBuilder> labelBuilder_;

      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public java.util.List<LabelPair> getLabelList() {
        if (labelBuilder_ == null) {
          return java.util.Collections.unmodifiableList(label_);
        } else {
          return labelBuilder_.getMessageList();
        }
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public int getLabelCount() {
        if (labelBuilder_ == null) {
          return label_.size();
        } else {
          return labelBuilder_.getCount();
        }
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public io.prometheus.client.Metrics.LabelPair getLabel(int index) {
        if (labelBuilder_ == null) {
          return label_.get(index);
        } else {
          return labelBuilder_.getMessage(index);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public Builder setLabel(
              int index, io.prometheus.client.Metrics.LabelPair value) {
        if (labelBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureLabelIsMutable();
          label_.set(index, value);
          onChanged();
        } else {
          labelBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public Builder setLabel(
              int index, io.prometheus.client.Metrics.LabelPair.Builder builderForValue) {
        if (labelBuilder_ == null) {
          ensureLabelIsMutable();
          label_.set(index, builderForValue.build());
          onChanged();
        } else {
          labelBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public Builder addLabel(io.prometheus.client.Metrics.LabelPair value) {
        if (labelBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureLabelIsMutable();
          label_.add(value);
          onChanged();
        } else {
          labelBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public Builder addLabel(
              int index, io.prometheus.client.Metrics.LabelPair value) {
        if (labelBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureLabelIsMutable();
          label_.add(index, value);
          onChanged();
        } else {
          labelBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public Builder addLabel(
              io.prometheus.client.Metrics.LabelPair.Builder builderForValue) {
        if (labelBuilder_ == null) {
          ensureLabelIsMutable();
          label_.add(builderForValue.build());
          onChanged();
        } else {
          labelBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public Builder addLabel(
              int index, io.prometheus.client.Metrics.LabelPair.Builder builderForValue) {
        if (labelBuilder_ == null) {
          ensureLabelIsMutable();
          label_.add(index, builderForValue.build());
          onChanged();
        } else {
          labelBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public Builder addAllLabel(
              Iterable<? extends LabelPair> values) {
        if (labelBuilder_ == null) {
          ensureLabelIsMutable();
          com.google.protobuf.AbstractMessageLite.Builder.addAll(
                  values, label_);
          onChanged();
        } else {
          labelBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public Builder clearLabel() {
        if (labelBuilder_ == null) {
          label_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000001);
          onChanged();
        } else {
          labelBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public Builder removeLabel(int index) {
        if (labelBuilder_ == null) {
          ensureLabelIsMutable();
          label_.remove(index);
          onChanged();
        } else {
          labelBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public io.prometheus.client.Metrics.LabelPair.Builder getLabelBuilder(
              int index) {
        return getLabelFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public io.prometheus.client.Metrics.LabelPairOrBuilder getLabelOrBuilder(
              int index) {
        if (labelBuilder_ == null) {
          return label_.get(index);  } else {
          return labelBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public java.util.List<? extends LabelPairOrBuilder>
      getLabelOrBuilderList() {
        if (labelBuilder_ != null) {
          return labelBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(label_);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public io.prometheus.client.Metrics.LabelPair.Builder addLabelBuilder() {
        return getLabelFieldBuilder().addBuilder(
                io.prometheus.client.Metrics.LabelPair.getDefaultInstance());
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public io.prometheus.client.Metrics.LabelPair.Builder addLabelBuilder(
              int index) {
        return getLabelFieldBuilder().addBuilder(
                index, io.prometheus.client.Metrics.LabelPair.getDefaultInstance());
      }
      /**
       * <code>repeated .io.prometheus.client.LabelPair label = 1;</code>
       */
      public java.util.List<LabelPair.Builder>
      getLabelBuilderList() {
        return getLabelFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilder<
              io.prometheus.client.Metrics.LabelPair, io.prometheus.client.Metrics.LabelPair.Builder, io.prometheus.client.Metrics.LabelPairOrBuilder>
      getLabelFieldBuilder() {
        if (labelBuilder_ == null) {
          labelBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
                  io.prometheus.client.Metrics.LabelPair, io.prometheus.client.Metrics.LabelPair.Builder, io.prometheus.client.Metrics.LabelPairOrBuilder>(
                  label_,
                  ((bitField0_ & 0x00000001) == 0x00000001),
                  getParentForChildren(),
                  isClean());
          label_ = null;
        }
        return labelBuilder_;
      }

      private io.prometheus.client.Metrics.Gauge gauge_ = io.prometheus.client.Metrics.Gauge.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
              io.prometheus.client.Metrics.Gauge, io.prometheus.client.Metrics.Gauge.Builder, io.prometheus.client.Metrics.GaugeOrBuilder> gaugeBuilder_;
      /**
       * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
       */
      public boolean hasGauge() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
       */
      public io.prometheus.client.Metrics.Gauge getGauge() {
        if (gaugeBuilder_ == null) {
          return gauge_;
        } else {
          return gaugeBuilder_.getMessage();
        }
      }
      /**
       * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
       */
      public Builder setGauge(io.prometheus.client.Metrics.Gauge value) {
        if (gaugeBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          gauge_ = value;
          onChanged();
        } else {
          gaugeBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
       */
      public Builder setGauge(
              io.prometheus.client.Metrics.Gauge.Builder builderForValue) {
        if (gaugeBuilder_ == null) {
          gauge_ = builderForValue.build();
          onChanged();
        } else {
          gaugeBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
       */
      public Builder mergeGauge(io.prometheus.client.Metrics.Gauge value) {
        if (gaugeBuilder_ == null) {
          if (((bitField0_ & 0x00000002) == 0x00000002) &&
                  gauge_ != io.prometheus.client.Metrics.Gauge.getDefaultInstance()) {
            gauge_ =
                    io.prometheus.client.Metrics.Gauge.newBuilder(gauge_).mergeFrom(value).buildPartial();
          } else {
            gauge_ = value;
          }
          onChanged();
        } else {
          gaugeBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
       */
      public Builder clearGauge() {
        if (gaugeBuilder_ == null) {
          gauge_ = io.prometheus.client.Metrics.Gauge.getDefaultInstance();
          onChanged();
        } else {
          gaugeBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
       */
      public io.prometheus.client.Metrics.Gauge.Builder getGaugeBuilder() {
        bitField0_ |= 0x00000002;
        onChanged();
        return getGaugeFieldBuilder().getBuilder();
      }
      /**
       * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
       */
      public io.prometheus.client.Metrics.GaugeOrBuilder getGaugeOrBuilder() {
        if (gaugeBuilder_ != null) {
          return gaugeBuilder_.getMessageOrBuilder();
        } else {
          return gauge_;
        }
      }
      /**
       * <code>optional .io.prometheus.client.Gauge gauge = 2;</code>
       */
      private com.google.protobuf.SingleFieldBuilder<
              io.prometheus.client.Metrics.Gauge, io.prometheus.client.Metrics.Gauge.Builder, io.prometheus.client.Metrics.GaugeOrBuilder>
      getGaugeFieldBuilder() {
        if (gaugeBuilder_ == null) {
          gaugeBuilder_ = new com.google.protobuf.SingleFieldBuilder<
                  io.prometheus.client.Metrics.Gauge, io.prometheus.client.Metrics.Gauge.Builder, io.prometheus.client.Metrics.GaugeOrBuilder>(
                  getGauge(),
                  getParentForChildren(),
                  isClean());
          gauge_ = null;
        }
        return gaugeBuilder_;
      }

      private io.prometheus.client.Metrics.Counter counter_ = io.prometheus.client.Metrics.Counter.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
              io.prometheus.client.Metrics.Counter, io.prometheus.client.Metrics.Counter.Builder, io.prometheus.client.Metrics.CounterOrBuilder> counterBuilder_;
      /**
       * <code>optional .io.prometheus.client.Counter counter = 3;</code>
       */
      public boolean hasCounter() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>optional .io.prometheus.client.Counter counter = 3;</code>
       */
      public io.prometheus.client.Metrics.Counter getCounter() {
        if (counterBuilder_ == null) {
          return counter_;
        } else {
          return counterBuilder_.getMessage();
        }
      }
      /**
       * <code>optional .io.prometheus.client.Counter counter = 3;</code>
       */
      public Builder setCounter(io.prometheus.client.Metrics.Counter value) {
        if (counterBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          counter_ = value;
          onChanged();
        } else {
          counterBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000004;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Counter counter = 3;</code>
       */
      public Builder setCounter(
              io.prometheus.client.Metrics.Counter.Builder builderForValue) {
        if (counterBuilder_ == null) {
          counter_ = builderForValue.build();
          onChanged();
        } else {
          counterBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000004;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Counter counter = 3;</code>
       */
      public Builder mergeCounter(io.prometheus.client.Metrics.Counter value) {
        if (counterBuilder_ == null) {
          if (((bitField0_ & 0x00000004) == 0x00000004) &&
                  counter_ != io.prometheus.client.Metrics.Counter.getDefaultInstance()) {
            counter_ =
                    io.prometheus.client.Metrics.Counter.newBuilder(counter_).mergeFrom(value).buildPartial();
          } else {
            counter_ = value;
          }
          onChanged();
        } else {
          counterBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000004;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Counter counter = 3;</code>
       */
      public Builder clearCounter() {
        if (counterBuilder_ == null) {
          counter_ = io.prometheus.client.Metrics.Counter.getDefaultInstance();
          onChanged();
        } else {
          counterBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000004);
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Counter counter = 3;</code>
       */
      public io.prometheus.client.Metrics.Counter.Builder getCounterBuilder() {
        bitField0_ |= 0x00000004;
        onChanged();
        return getCounterFieldBuilder().getBuilder();
      }
      /**
       * <code>optional .io.prometheus.client.Counter counter = 3;</code>
       */
      public io.prometheus.client.Metrics.CounterOrBuilder getCounterOrBuilder() {
        if (counterBuilder_ != null) {
          return counterBuilder_.getMessageOrBuilder();
        } else {
          return counter_;
        }
      }
      /**
       * <code>optional .io.prometheus.client.Counter counter = 3;</code>
       */
      private com.google.protobuf.SingleFieldBuilder<
              io.prometheus.client.Metrics.Counter, io.prometheus.client.Metrics.Counter.Builder, io.prometheus.client.Metrics.CounterOrBuilder>
      getCounterFieldBuilder() {
        if (counterBuilder_ == null) {
          counterBuilder_ = new com.google.protobuf.SingleFieldBuilder<
                  io.prometheus.client.Metrics.Counter, io.prometheus.client.Metrics.Counter.Builder, io.prometheus.client.Metrics.CounterOrBuilder>(
                  getCounter(),
                  getParentForChildren(),
                  isClean());
          counter_ = null;
        }
        return counterBuilder_;
      }

      private io.prometheus.client.Metrics.Summary summary_ = io.prometheus.client.Metrics.Summary.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
              io.prometheus.client.Metrics.Summary, io.prometheus.client.Metrics.Summary.Builder, io.prometheus.client.Metrics.SummaryOrBuilder> summaryBuilder_;
      /**
       * <code>optional .io.prometheus.client.Summary summary = 4;</code>
       */
      public boolean hasSummary() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      /**
       * <code>optional .io.prometheus.client.Summary summary = 4;</code>
       */
      public io.prometheus.client.Metrics.Summary getSummary() {
        if (summaryBuilder_ == null) {
          return summary_;
        } else {
          return summaryBuilder_.getMessage();
        }
      }
      /**
       * <code>optional .io.prometheus.client.Summary summary = 4;</code>
       */
      public Builder setSummary(io.prometheus.client.Metrics.Summary value) {
        if (summaryBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          summary_ = value;
          onChanged();
        } else {
          summaryBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000008;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Summary summary = 4;</code>
       */
      public Builder setSummary(
              io.prometheus.client.Metrics.Summary.Builder builderForValue) {
        if (summaryBuilder_ == null) {
          summary_ = builderForValue.build();
          onChanged();
        } else {
          summaryBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000008;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Summary summary = 4;</code>
       */
      public Builder mergeSummary(io.prometheus.client.Metrics.Summary value) {
        if (summaryBuilder_ == null) {
          if (((bitField0_ & 0x00000008) == 0x00000008) &&
                  summary_ != io.prometheus.client.Metrics.Summary.getDefaultInstance()) {
            summary_ =
                    io.prometheus.client.Metrics.Summary.newBuilder(summary_).mergeFrom(value).buildPartial();
          } else {
            summary_ = value;
          }
          onChanged();
        } else {
          summaryBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000008;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Summary summary = 4;</code>
       */
      public Builder clearSummary() {
        if (summaryBuilder_ == null) {
          summary_ = io.prometheus.client.Metrics.Summary.getDefaultInstance();
          onChanged();
        } else {
          summaryBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000008);
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Summary summary = 4;</code>
       */
      public io.prometheus.client.Metrics.Summary.Builder getSummaryBuilder() {
        bitField0_ |= 0x00000008;
        onChanged();
        return getSummaryFieldBuilder().getBuilder();
      }
      /**
       * <code>optional .io.prometheus.client.Summary summary = 4;</code>
       */
      public io.prometheus.client.Metrics.SummaryOrBuilder getSummaryOrBuilder() {
        if (summaryBuilder_ != null) {
          return summaryBuilder_.getMessageOrBuilder();
        } else {
          return summary_;
        }
      }
      /**
       * <code>optional .io.prometheus.client.Summary summary = 4;</code>
       */
      private com.google.protobuf.SingleFieldBuilder<
              io.prometheus.client.Metrics.Summary, io.prometheus.client.Metrics.Summary.Builder, io.prometheus.client.Metrics.SummaryOrBuilder>
      getSummaryFieldBuilder() {
        if (summaryBuilder_ == null) {
          summaryBuilder_ = new com.google.protobuf.SingleFieldBuilder<
                  io.prometheus.client.Metrics.Summary, io.prometheus.client.Metrics.Summary.Builder, io.prometheus.client.Metrics.SummaryOrBuilder>(
                  getSummary(),
                  getParentForChildren(),
                  isClean());
          summary_ = null;
        }
        return summaryBuilder_;
      }

      private io.prometheus.client.Metrics.Untyped untyped_ = io.prometheus.client.Metrics.Untyped.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
              io.prometheus.client.Metrics.Untyped, io.prometheus.client.Metrics.Untyped.Builder, io.prometheus.client.Metrics.UntypedOrBuilder> untypedBuilder_;
      /**
       * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
       */
      public boolean hasUntyped() {
        return ((bitField0_ & 0x00000010) == 0x00000010);
      }
      /**
       * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
       */
      public io.prometheus.client.Metrics.Untyped getUntyped() {
        if (untypedBuilder_ == null) {
          return untyped_;
        } else {
          return untypedBuilder_.getMessage();
        }
      }
      /**
       * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
       */
      public Builder setUntyped(io.prometheus.client.Metrics.Untyped value) {
        if (untypedBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          untyped_ = value;
          onChanged();
        } else {
          untypedBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000010;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
       */
      public Builder setUntyped(
              io.prometheus.client.Metrics.Untyped.Builder builderForValue) {
        if (untypedBuilder_ == null) {
          untyped_ = builderForValue.build();
          onChanged();
        } else {
          untypedBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000010;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
       */
      public Builder mergeUntyped(io.prometheus.client.Metrics.Untyped value) {
        if (untypedBuilder_ == null) {
          if (((bitField0_ & 0x00000010) == 0x00000010) &&
                  untyped_ != io.prometheus.client.Metrics.Untyped.getDefaultInstance()) {
            untyped_ =
                    io.prometheus.client.Metrics.Untyped.newBuilder(untyped_).mergeFrom(value).buildPartial();
          } else {
            untyped_ = value;
          }
          onChanged();
        } else {
          untypedBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000010;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
       */
      public Builder clearUntyped() {
        if (untypedBuilder_ == null) {
          untyped_ = io.prometheus.client.Metrics.Untyped.getDefaultInstance();
          onChanged();
        } else {
          untypedBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000010);
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
       */
      public io.prometheus.client.Metrics.Untyped.Builder getUntypedBuilder() {
        bitField0_ |= 0x00000010;
        onChanged();
        return getUntypedFieldBuilder().getBuilder();
      }
      /**
       * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
       */
      public io.prometheus.client.Metrics.UntypedOrBuilder getUntypedOrBuilder() {
        if (untypedBuilder_ != null) {
          return untypedBuilder_.getMessageOrBuilder();
        } else {
          return untyped_;
        }
      }
      /**
       * <code>optional .io.prometheus.client.Untyped untyped = 5;</code>
       */
      private com.google.protobuf.SingleFieldBuilder<
              io.prometheus.client.Metrics.Untyped, io.prometheus.client.Metrics.Untyped.Builder, io.prometheus.client.Metrics.UntypedOrBuilder>
      getUntypedFieldBuilder() {
        if (untypedBuilder_ == null) {
          untypedBuilder_ = new com.google.protobuf.SingleFieldBuilder<
                  io.prometheus.client.Metrics.Untyped, io.prometheus.client.Metrics.Untyped.Builder, io.prometheus.client.Metrics.UntypedOrBuilder>(
                  getUntyped(),
                  getParentForChildren(),
                  isClean());
          untyped_ = null;
        }
        return untypedBuilder_;
      }

      private io.prometheus.client.Metrics.Histogram histogram_ = io.prometheus.client.Metrics.Histogram.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
              io.prometheus.client.Metrics.Histogram, io.prometheus.client.Metrics.Histogram.Builder, io.prometheus.client.Metrics.HistogramOrBuilder> histogramBuilder_;
      /**
       * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
       */
      public boolean hasHistogram() {
        return ((bitField0_ & 0x00000020) == 0x00000020);
      }
      /**
       * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
       */
      public io.prometheus.client.Metrics.Histogram getHistogram() {
        if (histogramBuilder_ == null) {
          return histogram_;
        } else {
          return histogramBuilder_.getMessage();
        }
      }
      /**
       * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
       */
      public Builder setHistogram(io.prometheus.client.Metrics.Histogram value) {
        if (histogramBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          histogram_ = value;
          onChanged();
        } else {
          histogramBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000020;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
       */
      public Builder setHistogram(
              io.prometheus.client.Metrics.Histogram.Builder builderForValue) {
        if (histogramBuilder_ == null) {
          histogram_ = builderForValue.build();
          onChanged();
        } else {
          histogramBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000020;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
       */
      public Builder mergeHistogram(io.prometheus.client.Metrics.Histogram value) {
        if (histogramBuilder_ == null) {
          if (((bitField0_ & 0x00000020) == 0x00000020) &&
                  histogram_ != io.prometheus.client.Metrics.Histogram.getDefaultInstance()) {
            histogram_ =
                    io.prometheus.client.Metrics.Histogram.newBuilder(histogram_).mergeFrom(value).buildPartial();
          } else {
            histogram_ = value;
          }
          onChanged();
        } else {
          histogramBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000020;
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
       */
      public Builder clearHistogram() {
        if (histogramBuilder_ == null) {
          histogram_ = io.prometheus.client.Metrics.Histogram.getDefaultInstance();
          onChanged();
        } else {
          histogramBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000020);
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
       */
      public io.prometheus.client.Metrics.Histogram.Builder getHistogramBuilder() {
        bitField0_ |= 0x00000020;
        onChanged();
        return getHistogramFieldBuilder().getBuilder();
      }
      /**
       * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
       */
      public io.prometheus.client.Metrics.HistogramOrBuilder getHistogramOrBuilder() {
        if (histogramBuilder_ != null) {
          return histogramBuilder_.getMessageOrBuilder();
        } else {
          return histogram_;
        }
      }
      /**
       * <code>optional .io.prometheus.client.Histogram histogram = 7;</code>
       */
      private com.google.protobuf.SingleFieldBuilder<
              io.prometheus.client.Metrics.Histogram, io.prometheus.client.Metrics.Histogram.Builder, io.prometheus.client.Metrics.HistogramOrBuilder>
      getHistogramFieldBuilder() {
        if (histogramBuilder_ == null) {
          histogramBuilder_ = new com.google.protobuf.SingleFieldBuilder<
                  io.prometheus.client.Metrics.Histogram, io.prometheus.client.Metrics.Histogram.Builder, io.prometheus.client.Metrics.HistogramOrBuilder>(
                  getHistogram(),
                  getParentForChildren(),
                  isClean());
          histogram_ = null;
        }
        return histogramBuilder_;
      }

      private long timestampMs_ ;
      /**
       * <code>optional int64 timestamp_ms = 6;</code>
       */
      public boolean hasTimestampMs() {
        return ((bitField0_ & 0x00000040) == 0x00000040);
      }
      /**
       * <code>optional int64 timestamp_ms = 6;</code>
       */
      public long getTimestampMs() {
        return timestampMs_;
      }
      /**
       * <code>optional int64 timestamp_ms = 6;</code>
       */
      public Builder setTimestampMs(long value) {
        bitField0_ |= 0x00000040;
        timestampMs_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int64 timestamp_ms = 6;</code>
       */
      public Builder clearTimestampMs() {
        bitField0_ = (bitField0_ & ~0x00000040);
        timestampMs_ = 0L;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:io.prometheus.client.Metric)
    }

    static {
      defaultInstance = new Metric(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:io.prometheus.client.Metric)
  }

  public interface MetricFamilyOrBuilder extends
          // @@protoc_insertion_point(interface_extends:io.prometheus.client.MetricFamily)
          com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional string name = 1;</code>
     */
    boolean hasName();
    /**
     * <code>optional string name = 1;</code>
     */
    String getName();
    /**
     * <code>optional string name = 1;</code>
     */
    com.google.protobuf.ByteString
    getNameBytes();

    /**
     * <code>optional string help = 2;</code>
     */
    boolean hasHelp();
    /**
     * <code>optional string help = 2;</code>
     */
    String getHelp();
    /**
     * <code>optional string help = 2;</code>
     */
    com.google.protobuf.ByteString
    getHelpBytes();

    /**
     * <code>optional .io.prometheus.client.MetricType type = 3;</code>
     */
    boolean hasType();
    /**
     * <code>optional .io.prometheus.client.MetricType type = 3;</code>
     */
    io.prometheus.client.Metrics.MetricType getType();

    /**
     * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
     */
    java.util.List<Metric>
    getMetricList();
    /**
     * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
     */
    io.prometheus.client.Metrics.Metric getMetric(int index);
    /**
     * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
     */
    int getMetricCount();
    /**
     * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
     */
    java.util.List<? extends MetricOrBuilder>
    getMetricOrBuilderList();
    /**
     * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
     */
    io.prometheus.client.Metrics.MetricOrBuilder getMetricOrBuilder(
            int index);
  }
  /**
   * Protobuf type {@code io.prometheus.client.MetricFamily}
   */
  public static final class MetricFamily extends
          com.google.protobuf.GeneratedMessage implements
          // @@protoc_insertion_point(message_implements:io.prometheus.client.MetricFamily)
          MetricFamilyOrBuilder {
    // Use MetricFamily.newBuilder() to construct.
    private MetricFamily(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private MetricFamily(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final MetricFamily defaultInstance;
    public static MetricFamily getDefaultInstance() {
      return defaultInstance;
    }

    public MetricFamily getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private MetricFamily(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
              com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                      extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000001;
              name_ = bs;
              break;
            }
            case 18: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000002;
              help_ = bs;
              break;
            }
            case 24: {
              int rawValue = input.readEnum();
              io.prometheus.client.Metrics.MetricType value = io.prometheus.client.Metrics.MetricType.valueOf(rawValue);
              if (value == null) {
                unknownFields.mergeVarintField(3, rawValue);
              } else {
                bitField0_ |= 0x00000004;
                type_ = value;
              }
              break;
            }
            case 34: {
              if (!((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
                metric_ = new java.util.ArrayList<Metric>();
                mutable_bitField0_ |= 0x00000008;
              }
              metric_.add(input.readMessage(io.prometheus.client.Metrics.Metric.PARSER, extensionRegistry));
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
          metric_ = java.util.Collections.unmodifiableList(metric_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_MetricFamily_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
    internalGetFieldAccessorTable() {
      return io.prometheus.client.Metrics.internal_static_io_prometheus_client_MetricFamily_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                      io.prometheus.client.Metrics.MetricFamily.class, io.prometheus.client.Metrics.MetricFamily.Builder.class);
    }

    public static com.google.protobuf.Parser<MetricFamily> PARSER =
            new com.google.protobuf.AbstractParser<MetricFamily>() {
              public MetricFamily parsePartialFrom(
                      com.google.protobuf.CodedInputStream input,
                      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                      throws com.google.protobuf.InvalidProtocolBufferException {
                return new MetricFamily(input, extensionRegistry);
              }
            };

    @Override
    public com.google.protobuf.Parser<MetricFamily> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int NAME_FIELD_NUMBER = 1;
    private Object name_;
    /**
     * <code>optional string name = 1;</code>
     */
    public boolean hasName() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional string name = 1;</code>
     */
    public String getName() {
      Object ref = name_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          name_ = s;
        }
        return s;
      }
    }
    /**
     * <code>optional string name = 1;</code>
     */
    public com.google.protobuf.ByteString
    getNameBytes() {
      Object ref = name_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (String) ref);
        name_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int HELP_FIELD_NUMBER = 2;
    private Object help_;
    /**
     * <code>optional string help = 2;</code>
     */
    public boolean hasHelp() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional string help = 2;</code>
     */
    public String getHelp() {
      Object ref = help_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          help_ = s;
        }
        return s;
      }
    }
    /**
     * <code>optional string help = 2;</code>
     */
    public com.google.protobuf.ByteString
    getHelpBytes() {
      Object ref = help_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
                com.google.protobuf.ByteString.copyFromUtf8(
                        (String) ref);
        help_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int TYPE_FIELD_NUMBER = 3;
    private io.prometheus.client.Metrics.MetricType type_;
    /**
     * <code>optional .io.prometheus.client.MetricType type = 3;</code>
     */
    public boolean hasType() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>optional .io.prometheus.client.MetricType type = 3;</code>
     */
    public io.prometheus.client.Metrics.MetricType getType() {
      return type_;
    }

    public static final int METRIC_FIELD_NUMBER = 4;
    private java.util.List<Metric> metric_;
    /**
     * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
     */
    public java.util.List<Metric> getMetricList() {
      return metric_;
    }
    /**
     * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
     */
    public java.util.List<? extends MetricOrBuilder>
    getMetricOrBuilderList() {
      return metric_;
    }
    /**
     * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
     */
    public int getMetricCount() {
      return metric_.size();
    }
    /**
     * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
     */
    public io.prometheus.client.Metrics.Metric getMetric(int index) {
      return metric_.get(index);
    }
    /**
     * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
     */
    public io.prometheus.client.Metrics.MetricOrBuilder getMetricOrBuilder(
            int index) {
      return metric_.get(index);
    }

    private void initFields() {
      name_ = "";
      help_ = "";
      type_ = io.prometheus.client.Metrics.MetricType.COUNTER;
      metric_ = java.util.Collections.emptyList();
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getNameBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getHelpBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeEnum(3, type_.getNumber());
      }
      for (int i = 0; i < metric_.size(); i++) {
        output.writeMessage(4, metric_.get(i));
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
                .computeBytesSize(1, getNameBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
                .computeBytesSize(2, getHelpBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
                .computeEnumSize(3, type_.getNumber());
      }
      for (int i = 0; i < metric_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(4, metric_.get(i));
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
            throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static io.prometheus.client.Metrics.MetricFamily parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.MetricFamily parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.MetricFamily parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.prometheus.client.Metrics.MetricFamily parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.MetricFamily parseFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.MetricFamily parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.MetricFamily parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static io.prometheus.client.Metrics.MetricFamily parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static io.prometheus.client.Metrics.MetricFamily parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static io.prometheus.client.Metrics.MetricFamily parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(io.prometheus.client.Metrics.MetricFamily prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code io.prometheus.client.MetricFamily}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessage.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:io.prometheus.client.MetricFamily)
            io.prometheus.client.Metrics.MetricFamilyOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_MetricFamily_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_MetricFamily_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        io.prometheus.client.Metrics.MetricFamily.class, io.prometheus.client.Metrics.MetricFamily.Builder.class);
      }

      // Construct using io.prometheus.client.Metrics.MetricFamily.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
              com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          getMetricFieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        name_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        help_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        type_ = io.prometheus.client.Metrics.MetricType.COUNTER;
        bitField0_ = (bitField0_ & ~0x00000004);
        if (metricBuilder_ == null) {
          metric_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000008);
        } else {
          metricBuilder_.clear();
        }
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
      getDescriptorForType() {
        return io.prometheus.client.Metrics.internal_static_io_prometheus_client_MetricFamily_descriptor;
      }

      public io.prometheus.client.Metrics.MetricFamily getDefaultInstanceForType() {
        return io.prometheus.client.Metrics.MetricFamily.getDefaultInstance();
      }

      public io.prometheus.client.Metrics.MetricFamily build() {
        io.prometheus.client.Metrics.MetricFamily result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public io.prometheus.client.Metrics.MetricFamily buildPartial() {
        io.prometheus.client.Metrics.MetricFamily result = new io.prometheus.client.Metrics.MetricFamily(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.name_ = name_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.help_ = help_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.type_ = type_;
        if (metricBuilder_ == null) {
          if (((bitField0_ & 0x00000008) == 0x00000008)) {
            metric_ = java.util.Collections.unmodifiableList(metric_);
            bitField0_ = (bitField0_ & ~0x00000008);
          }
          result.metric_ = metric_;
        } else {
          result.metric_ = metricBuilder_.build();
        }
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.prometheus.client.Metrics.MetricFamily) {
          return mergeFrom((io.prometheus.client.Metrics.MetricFamily)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.prometheus.client.Metrics.MetricFamily other) {
        if (other == io.prometheus.client.Metrics.MetricFamily.getDefaultInstance()) return this;
        if (other.hasName()) {
          bitField0_ |= 0x00000001;
          name_ = other.name_;
          onChanged();
        }
        if (other.hasHelp()) {
          bitField0_ |= 0x00000002;
          help_ = other.help_;
          onChanged();
        }
        if (other.hasType()) {
          setType(other.getType());
        }
        if (metricBuilder_ == null) {
          if (!other.metric_.isEmpty()) {
            if (metric_.isEmpty()) {
              metric_ = other.metric_;
              bitField0_ = (bitField0_ & ~0x00000008);
            } else {
              ensureMetricIsMutable();
              metric_.addAll(other.metric_);
            }
            onChanged();
          }
        } else {
          if (!other.metric_.isEmpty()) {
            if (metricBuilder_.isEmpty()) {
              metricBuilder_.dispose();
              metricBuilder_ = null;
              metric_ = other.metric_;
              bitField0_ = (bitField0_ & ~0x00000008);
              metricBuilder_ =
                      com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                              getMetricFieldBuilder() : null;
            } else {
              metricBuilder_.addAllMessages(other.metric_);
            }
          }
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
              com.google.protobuf.CodedInputStream input,
              com.google.protobuf.ExtensionRegistryLite extensionRegistry)
              throws java.io.IOException {
        io.prometheus.client.Metrics.MetricFamily parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.prometheus.client.Metrics.MetricFamily) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private Object name_ = "";
      /**
       * <code>optional string name = 1;</code>
       */
      public boolean hasName() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional string name = 1;</code>
       */
      public String getName() {
        Object ref = name_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
                  (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            name_ = s;
          }
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>optional string name = 1;</code>
       */
      public com.google.protobuf.ByteString
      getNameBytes() {
        Object ref = name_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b =
                  com.google.protobuf.ByteString.copyFromUtf8(
                          (String) ref);
          name_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string name = 1;</code>
       */
      public Builder setName(
              String value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000001;
        name_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional string name = 1;</code>
       */
      public Builder clearName() {
        bitField0_ = (bitField0_ & ~0x00000001);
        name_ = getDefaultInstance().getName();
        onChanged();
        return this;
      }
      /**
       * <code>optional string name = 1;</code>
       */
      public Builder setNameBytes(
              com.google.protobuf.ByteString value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000001;
        name_ = value;
        onChanged();
        return this;
      }

      private Object help_ = "";
      /**
       * <code>optional string help = 2;</code>
       */
      public boolean hasHelp() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional string help = 2;</code>
       */
      public String getHelp() {
        Object ref = help_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
                  (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            help_ = s;
          }
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>optional string help = 2;</code>
       */
      public com.google.protobuf.ByteString
      getHelpBytes() {
        Object ref = help_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b =
                  com.google.protobuf.ByteString.copyFromUtf8(
                          (String) ref);
          help_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string help = 2;</code>
       */
      public Builder setHelp(
              String value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000002;
        help_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional string help = 2;</code>
       */
      public Builder clearHelp() {
        bitField0_ = (bitField0_ & ~0x00000002);
        help_ = getDefaultInstance().getHelp();
        onChanged();
        return this;
      }
      /**
       * <code>optional string help = 2;</code>
       */
      public Builder setHelpBytes(
              com.google.protobuf.ByteString value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000002;
        help_ = value;
        onChanged();
        return this;
      }

      private io.prometheus.client.Metrics.MetricType type_ = io.prometheus.client.Metrics.MetricType.COUNTER;
      /**
       * <code>optional .io.prometheus.client.MetricType type = 3;</code>
       */
      public boolean hasType() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>optional .io.prometheus.client.MetricType type = 3;</code>
       */
      public io.prometheus.client.Metrics.MetricType getType() {
        return type_;
      }
      /**
       * <code>optional .io.prometheus.client.MetricType type = 3;</code>
       */
      public Builder setType(io.prometheus.client.Metrics.MetricType value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000004;
        type_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional .io.prometheus.client.MetricType type = 3;</code>
       */
      public Builder clearType() {
        bitField0_ = (bitField0_ & ~0x00000004);
        type_ = io.prometheus.client.Metrics.MetricType.COUNTER;
        onChanged();
        return this;
      }

      private java.util.List<Metric> metric_ =
              java.util.Collections.emptyList();
      private void ensureMetricIsMutable() {
        if (!((bitField0_ & 0x00000008) == 0x00000008)) {
          metric_ = new java.util.ArrayList<Metric>(metric_);
          bitField0_ |= 0x00000008;
        }
      }

      private com.google.protobuf.RepeatedFieldBuilder<
              io.prometheus.client.Metrics.Metric, io.prometheus.client.Metrics.Metric.Builder, io.prometheus.client.Metrics.MetricOrBuilder> metricBuilder_;

      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public java.util.List<Metric> getMetricList() {
        if (metricBuilder_ == null) {
          return java.util.Collections.unmodifiableList(metric_);
        } else {
          return metricBuilder_.getMessageList();
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public int getMetricCount() {
        if (metricBuilder_ == null) {
          return metric_.size();
        } else {
          return metricBuilder_.getCount();
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public io.prometheus.client.Metrics.Metric getMetric(int index) {
        if (metricBuilder_ == null) {
          return metric_.get(index);
        } else {
          return metricBuilder_.getMessage(index);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public Builder setMetric(
              int index, io.prometheus.client.Metrics.Metric value) {
        if (metricBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureMetricIsMutable();
          metric_.set(index, value);
          onChanged();
        } else {
          metricBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public Builder setMetric(
              int index, io.prometheus.client.Metrics.Metric.Builder builderForValue) {
        if (metricBuilder_ == null) {
          ensureMetricIsMutable();
          metric_.set(index, builderForValue.build());
          onChanged();
        } else {
          metricBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public Builder addMetric(io.prometheus.client.Metrics.Metric value) {
        if (metricBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureMetricIsMutable();
          metric_.add(value);
          onChanged();
        } else {
          metricBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public Builder addMetric(
              int index, io.prometheus.client.Metrics.Metric value) {
        if (metricBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureMetricIsMutable();
          metric_.add(index, value);
          onChanged();
        } else {
          metricBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public Builder addMetric(
              io.prometheus.client.Metrics.Metric.Builder builderForValue) {
        if (metricBuilder_ == null) {
          ensureMetricIsMutable();
          metric_.add(builderForValue.build());
          onChanged();
        } else {
          metricBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public Builder addMetric(
              int index, io.prometheus.client.Metrics.Metric.Builder builderForValue) {
        if (metricBuilder_ == null) {
          ensureMetricIsMutable();
          metric_.add(index, builderForValue.build());
          onChanged();
        } else {
          metricBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public Builder addAllMetric(
              Iterable<? extends Metric> values) {
        if (metricBuilder_ == null) {
          ensureMetricIsMutable();
          com.google.protobuf.AbstractMessageLite.Builder.addAll(
                  values, metric_);
          onChanged();
        } else {
          metricBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public Builder clearMetric() {
        if (metricBuilder_ == null) {
          metric_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000008);
          onChanged();
        } else {
          metricBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public Builder removeMetric(int index) {
        if (metricBuilder_ == null) {
          ensureMetricIsMutable();
          metric_.remove(index);
          onChanged();
        } else {
          metricBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public io.prometheus.client.Metrics.Metric.Builder getMetricBuilder(
              int index) {
        return getMetricFieldBuilder().getBuilder(index);
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public io.prometheus.client.Metrics.MetricOrBuilder getMetricOrBuilder(
              int index) {
        if (metricBuilder_ == null) {
          return metric_.get(index);  } else {
          return metricBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public java.util.List<? extends MetricOrBuilder>
      getMetricOrBuilderList() {
        if (metricBuilder_ != null) {
          return metricBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(metric_);
        }
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public io.prometheus.client.Metrics.Metric.Builder addMetricBuilder() {
        return getMetricFieldBuilder().addBuilder(
                io.prometheus.client.Metrics.Metric.getDefaultInstance());
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public io.prometheus.client.Metrics.Metric.Builder addMetricBuilder(
              int index) {
        return getMetricFieldBuilder().addBuilder(
                index, io.prometheus.client.Metrics.Metric.getDefaultInstance());
      }
      /**
       * <code>repeated .io.prometheus.client.Metric metric = 4;</code>
       */
      public java.util.List<Metric.Builder>
      getMetricBuilderList() {
        return getMetricFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilder<
              io.prometheus.client.Metrics.Metric, io.prometheus.client.Metrics.Metric.Builder, io.prometheus.client.Metrics.MetricOrBuilder>
      getMetricFieldBuilder() {
        if (metricBuilder_ == null) {
          metricBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
                  io.prometheus.client.Metrics.Metric, io.prometheus.client.Metrics.Metric.Builder, io.prometheus.client.Metrics.MetricOrBuilder>(
                  metric_,
                  ((bitField0_ & 0x00000008) == 0x00000008),
                  getParentForChildren(),
                  isClean());
          metric_ = null;
        }
        return metricBuilder_;
      }

      // @@protoc_insertion_point(builder_scope:io.prometheus.client.MetricFamily)
    }

    static {
      defaultInstance = new MetricFamily(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:io.prometheus.client.MetricFamily)
  }

  private static final com.google.protobuf.Descriptors.Descriptor
          internal_static_io_prometheus_client_LabelPair_descriptor;
  private static
  com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internal_static_io_prometheus_client_LabelPair_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
          internal_static_io_prometheus_client_Gauge_descriptor;
  private static
  com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internal_static_io_prometheus_client_Gauge_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
          internal_static_io_prometheus_client_Counter_descriptor;
  private static
  com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internal_static_io_prometheus_client_Counter_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
          internal_static_io_prometheus_client_Quantile_descriptor;
  private static
  com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internal_static_io_prometheus_client_Quantile_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
          internal_static_io_prometheus_client_Summary_descriptor;
  private static
  com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internal_static_io_prometheus_client_Summary_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
          internal_static_io_prometheus_client_Untyped_descriptor;
  private static
  com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internal_static_io_prometheus_client_Untyped_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
          internal_static_io_prometheus_client_Histogram_descriptor;
  private static
  com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internal_static_io_prometheus_client_Histogram_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
          internal_static_io_prometheus_client_Bucket_descriptor;
  private static
  com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internal_static_io_prometheus_client_Bucket_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
          internal_static_io_prometheus_client_Metric_descriptor;
  private static
  com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internal_static_io_prometheus_client_Metric_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
          internal_static_io_prometheus_client_MetricFamily_descriptor;
  private static
  com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internal_static_io_prometheus_client_MetricFamily_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
  getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
          descriptor;
  static {
    String[] descriptorData = {
            "\n\rmetrics.proto\022\024io.prometheus.client\"(\n" +
                    "\tLabelPair\022\014\n\004name\030\001 \001(\t\022\r\n\005value\030\002 \001(\t\"" +
                    "\026\n\005Gauge\022\r\n\005value\030\001 \001(\001\"\030\n\007Counter\022\r\n\005va" +
                    "lue\030\001 \001(\001\"+\n\010Quantile\022\020\n\010quantile\030\001 \001(\001\022" +
                    "\r\n\005value\030\002 \001(\001\"e\n\007Summary\022\024\n\014sample_coun" +
                    "t\030\001 \001(\004\022\022\n\nsample_sum\030\002 \001(\001\0220\n\010quantile\030" +
                    "\003 \003(\0132\036.io.prometheus.client.Quantile\"\030\n" +
                    "\007Untyped\022\r\n\005value\030\001 \001(\001\"c\n\tHistogram\022\024\n\014" +
                    "sample_count\030\001 \001(\004\022\022\n\nsample_sum\030\002 \001(\001\022," +
                    "\n\006bucket\030\003 \003(\0132\034.io.prometheus.client.Bu",
            "cket\"7\n\006Bucket\022\030\n\020cumulative_count\030\001 \001(\004" +
                    "\022\023\n\013upper_bound\030\002 \001(\001\"\276\002\n\006Metric\022.\n\005labe" +
                    "l\030\001 \003(\0132\037.io.prometheus.client.LabelPair" +
                    "\022*\n\005gauge\030\002 \001(\0132\033.io.prometheus.client.G" +
                    "auge\022.\n\007counter\030\003 \001(\0132\035.io.prometheus.cl" +
                    "ient.Counter\022.\n\007summary\030\004 \001(\0132\035.io.prome" +
                    "theus.client.Summary\022.\n\007untyped\030\005 \001(\0132\035." +
                    "io.prometheus.client.Untyped\0222\n\thistogra" +
                    "m\030\007 \001(\0132\037.io.prometheus.client.Histogram" +
                    "\022\024\n\014timestamp_ms\030\006 \001(\003\"\210\001\n\014MetricFamily\022",
            "\014\n\004name\030\001 \001(\t\022\014\n\004help\030\002 \001(\t\022.\n\004type\030\003 \001(" +
                    "\0162 .io.prometheus.client.MetricType\022,\n\006m" +
                    "etric\030\004 \003(\0132\034.io.prometheus.client.Metri" +
                    "c*M\n\nMetricType\022\013\n\007COUNTER\020\000\022\t\n\005GAUGE\020\001\022" +
                    "\013\n\007SUMMARY\020\002\022\013\n\007UNTYPED\020\003\022\r\n\tHISTOGRAM\020\004" +
                    "B\026\n\024io.prometheus.client"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
            new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
              public com.google.protobuf.ExtensionRegistry assignDescriptors(
                      com.google.protobuf.Descriptors.FileDescriptor root) {
                descriptor = root;
                return null;
              }
            };
    com.google.protobuf.Descriptors.FileDescriptor
            .internalBuildGeneratedFileFrom(descriptorData,
                    new com.google.protobuf.Descriptors.FileDescriptor[] {
                    }, assigner);
    internal_static_io_prometheus_client_LabelPair_descriptor =
            getDescriptor().getMessageTypes().get(0);
    internal_static_io_prometheus_client_LabelPair_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
            internal_static_io_prometheus_client_LabelPair_descriptor,
            new String[] { "Name", "Value", });
    internal_static_io_prometheus_client_Gauge_descriptor =
            getDescriptor().getMessageTypes().get(1);
    internal_static_io_prometheus_client_Gauge_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
            internal_static_io_prometheus_client_Gauge_descriptor,
            new String[] { "Value", });
    internal_static_io_prometheus_client_Counter_descriptor =
            getDescriptor().getMessageTypes().get(2);
    internal_static_io_prometheus_client_Counter_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
            internal_static_io_prometheus_client_Counter_descriptor,
            new String[] { "Value", });
    internal_static_io_prometheus_client_Quantile_descriptor =
            getDescriptor().getMessageTypes().get(3);
    internal_static_io_prometheus_client_Quantile_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
            internal_static_io_prometheus_client_Quantile_descriptor,
            new String[] { "Quantile", "Value", });
    internal_static_io_prometheus_client_Summary_descriptor =
            getDescriptor().getMessageTypes().get(4);
    internal_static_io_prometheus_client_Summary_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
            internal_static_io_prometheus_client_Summary_descriptor,
            new String[] { "SampleCount", "SampleSum", "Quantile", });
    internal_static_io_prometheus_client_Untyped_descriptor =
            getDescriptor().getMessageTypes().get(5);
    internal_static_io_prometheus_client_Untyped_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
            internal_static_io_prometheus_client_Untyped_descriptor,
            new String[] { "Value", });
    internal_static_io_prometheus_client_Histogram_descriptor =
            getDescriptor().getMessageTypes().get(6);
    internal_static_io_prometheus_client_Histogram_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
            internal_static_io_prometheus_client_Histogram_descriptor,
            new String[] { "SampleCount", "SampleSum", "Bucket", });
    internal_static_io_prometheus_client_Bucket_descriptor =
            getDescriptor().getMessageTypes().get(7);
    internal_static_io_prometheus_client_Bucket_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
            internal_static_io_prometheus_client_Bucket_descriptor,
            new String[] { "CumulativeCount", "UpperBound", });
    internal_static_io_prometheus_client_Metric_descriptor =
            getDescriptor().getMessageTypes().get(8);
    internal_static_io_prometheus_client_Metric_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
            internal_static_io_prometheus_client_Metric_descriptor,
            new String[] { "Label", "Gauge", "Counter", "Summary", "Untyped", "Histogram", "TimestampMs", });
    internal_static_io_prometheus_client_MetricFamily_descriptor =
            getDescriptor().getMessageTypes().get(9);
    internal_static_io_prometheus_client_MetricFamily_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
            internal_static_io_prometheus_client_MetricFamily_descriptor,
            new String[] { "Name", "Help", "Type", "Metric", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}