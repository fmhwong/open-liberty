// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Store.proto

package com.ibm.test.g3store.grpc;

/**
 * Protobuf type {@code Price}
 */
public  final class Price extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:Price)
    PriceOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Price.newBuilder() to construct.
  private Price(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Price() {
    purchaseType_ = 0;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Price();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Price(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
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
          case 8: {
            int rawValue = input.readEnum();

            purchaseType_ = rawValue;
            break;
          }
          case 17: {

            sellingPrice_ = input.readDouble();
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.ibm.test.g3store.grpc.StoreProto.internal_static_Price_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.ibm.test.g3store.grpc.StoreProto.internal_static_Price_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.ibm.test.g3store.grpc.Price.class, com.ibm.test.g3store.grpc.Price.Builder.class);
  }

  public static final int PURCHASETYPE_FIELD_NUMBER = 1;
  private int purchaseType_;
  /**
   * <code>.PurchaseType purchaseType = 1;</code>
   * @return The enum numeric value on the wire for purchaseType.
   */
  public int getPurchaseTypeValue() {
    return purchaseType_;
  }
  /**
   * <code>.PurchaseType purchaseType = 1;</code>
   * @return The purchaseType.
   */
  public com.ibm.test.g3store.grpc.PurchaseType getPurchaseType() {
    @SuppressWarnings("deprecation")
    com.ibm.test.g3store.grpc.PurchaseType result = com.ibm.test.g3store.grpc.PurchaseType.valueOf(purchaseType_);
    return result == null ? com.ibm.test.g3store.grpc.PurchaseType.UNRECOGNIZED : result;
  }

  public static final int SELLINGPRICE_FIELD_NUMBER = 2;
  private double sellingPrice_;
  /**
   * <code>double sellingPrice = 2;</code>
   * @return The sellingPrice.
   */
  public double getSellingPrice() {
    return sellingPrice_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (purchaseType_ != com.ibm.test.g3store.grpc.PurchaseType.BLUEPOINTS.getNumber()) {
      output.writeEnum(1, purchaseType_);
    }
    if (sellingPrice_ != 0D) {
      output.writeDouble(2, sellingPrice_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (purchaseType_ != com.ibm.test.g3store.grpc.PurchaseType.BLUEPOINTS.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1, purchaseType_);
    }
    if (sellingPrice_ != 0D) {
      size += com.google.protobuf.CodedOutputStream
        .computeDoubleSize(2, sellingPrice_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.ibm.test.g3store.grpc.Price)) {
      return super.equals(obj);
    }
    com.ibm.test.g3store.grpc.Price other = (com.ibm.test.g3store.grpc.Price) obj;

    if (purchaseType_ != other.purchaseType_) return false;
    if (java.lang.Double.doubleToLongBits(getSellingPrice())
        != java.lang.Double.doubleToLongBits(
            other.getSellingPrice())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + PURCHASETYPE_FIELD_NUMBER;
    hash = (53 * hash) + purchaseType_;
    hash = (37 * hash) + SELLINGPRICE_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        java.lang.Double.doubleToLongBits(getSellingPrice()));
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.ibm.test.g3store.grpc.Price parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibm.test.g3store.grpc.Price parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibm.test.g3store.grpc.Price parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibm.test.g3store.grpc.Price parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibm.test.g3store.grpc.Price parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.ibm.test.g3store.grpc.Price parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.ibm.test.g3store.grpc.Price parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ibm.test.g3store.grpc.Price parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ibm.test.g3store.grpc.Price parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.ibm.test.g3store.grpc.Price parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.ibm.test.g3store.grpc.Price parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.ibm.test.g3store.grpc.Price parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.ibm.test.g3store.grpc.Price prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code Price}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:Price)
      com.ibm.test.g3store.grpc.PriceOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.ibm.test.g3store.grpc.StoreProto.internal_static_Price_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.ibm.test.g3store.grpc.StoreProto.internal_static_Price_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.ibm.test.g3store.grpc.Price.class, com.ibm.test.g3store.grpc.Price.Builder.class);
    }

    // Construct using com.ibm.test.g3store.grpc.Price.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      purchaseType_ = 0;

      sellingPrice_ = 0D;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.ibm.test.g3store.grpc.StoreProto.internal_static_Price_descriptor;
    }

    @java.lang.Override
    public com.ibm.test.g3store.grpc.Price getDefaultInstanceForType() {
      return com.ibm.test.g3store.grpc.Price.getDefaultInstance();
    }

    @java.lang.Override
    public com.ibm.test.g3store.grpc.Price build() {
      com.ibm.test.g3store.grpc.Price result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.ibm.test.g3store.grpc.Price buildPartial() {
      com.ibm.test.g3store.grpc.Price result = new com.ibm.test.g3store.grpc.Price(this);
      result.purchaseType_ = purchaseType_;
      result.sellingPrice_ = sellingPrice_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.ibm.test.g3store.grpc.Price) {
        return mergeFrom((com.ibm.test.g3store.grpc.Price)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.ibm.test.g3store.grpc.Price other) {
      if (other == com.ibm.test.g3store.grpc.Price.getDefaultInstance()) return this;
      if (other.purchaseType_ != 0) {
        setPurchaseTypeValue(other.getPurchaseTypeValue());
      }
      if (other.getSellingPrice() != 0D) {
        setSellingPrice(other.getSellingPrice());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.ibm.test.g3store.grpc.Price parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.ibm.test.g3store.grpc.Price) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private int purchaseType_ = 0;
    /**
     * <code>.PurchaseType purchaseType = 1;</code>
     * @return The enum numeric value on the wire for purchaseType.
     */
    public int getPurchaseTypeValue() {
      return purchaseType_;
    }
    /**
     * <code>.PurchaseType purchaseType = 1;</code>
     * @param value The enum numeric value on the wire for purchaseType to set.
     * @return This builder for chaining.
     */
    public Builder setPurchaseTypeValue(int value) {
      purchaseType_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.PurchaseType purchaseType = 1;</code>
     * @return The purchaseType.
     */
    public com.ibm.test.g3store.grpc.PurchaseType getPurchaseType() {
      @SuppressWarnings("deprecation")
      com.ibm.test.g3store.grpc.PurchaseType result = com.ibm.test.g3store.grpc.PurchaseType.valueOf(purchaseType_);
      return result == null ? com.ibm.test.g3store.grpc.PurchaseType.UNRECOGNIZED : result;
    }
    /**
     * <code>.PurchaseType purchaseType = 1;</code>
     * @param value The purchaseType to set.
     * @return This builder for chaining.
     */
    public Builder setPurchaseType(com.ibm.test.g3store.grpc.PurchaseType value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      purchaseType_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.PurchaseType purchaseType = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearPurchaseType() {
      
      purchaseType_ = 0;
      onChanged();
      return this;
    }

    private double sellingPrice_ ;
    /**
     * <code>double sellingPrice = 2;</code>
     * @return The sellingPrice.
     */
    public double getSellingPrice() {
      return sellingPrice_;
    }
    /**
     * <code>double sellingPrice = 2;</code>
     * @param value The sellingPrice to set.
     * @return This builder for chaining.
     */
    public Builder setSellingPrice(double value) {
      
      sellingPrice_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>double sellingPrice = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearSellingPrice() {
      
      sellingPrice_ = 0D;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:Price)
  }

  // @@protoc_insertion_point(class_scope:Price)
  private static final com.ibm.test.g3store.grpc.Price DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.ibm.test.g3store.grpc.Price();
  }

  public static com.ibm.test.g3store.grpc.Price getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Price>
      PARSER = new com.google.protobuf.AbstractParser<Price>() {
    @java.lang.Override
    public Price parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Price(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Price> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Price> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.ibm.test.g3store.grpc.Price getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

