package com.olam.digital.ofis.agrisupplier.dto;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class SupplierDto implements Parcelable {

    @SerializedName("id")
    private Long id;
    @SerializedName("supplierName")
    private String supplierName;
    @SerializedName("supplierId")
    private Long supplierId;
    @SerializedName("primaryPhoneNumber")
    private String primaryPhoneNumber;
    @SerializedName("secondaryPhoneNumber")
    private String secondaryPhoneNumber;
    @SerializedName("supplierGroupId")
    private Integer supplierGroupId;
    @SerializedName("supplierTypeId")
    private Integer supplierTypeId;
    @SerializedName("countryId")
    private Integer countryId;
    @SerializedName("productId")
    private Integer productId;

    public SupplierDto(Long id, String supplierName, Long supplierId, String primaryPhoneNumber, String secondaryPhoneNumber, Integer supplierGroupId, Integer supplierTypeId, Integer countryId, Integer productId) {
        this.id = id;
        this.supplierName = supplierName;
        this.supplierId = supplierId;
        this.primaryPhoneNumber = primaryPhoneNumber;
        this.secondaryPhoneNumber = secondaryPhoneNumber;
        this.supplierGroupId = supplierGroupId;
        this.supplierTypeId = supplierTypeId;
        this.countryId = countryId;
        this.productId = productId;
    }


    protected SupplierDto(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        supplierName = in.readString();
        if (in.readByte() == 0) {
            supplierId = null;
        } else {
            supplierId = in.readLong();
        }
        primaryPhoneNumber = in.readString();
        secondaryPhoneNumber = in.readString();
        if (in.readByte() == 0) {
            supplierGroupId = null;
        } else {
            supplierGroupId = in.readInt();
        }
        if (in.readByte() == 0) {
            supplierTypeId = null;
        } else {
            supplierTypeId = in.readInt();
        }
        if (in.readByte() == 0) {
            countryId = null;
        } else {
            countryId = in.readInt();
        }
        if (in.readByte() == 0) {
            productId = null;
        } else {
            productId = in.readInt();
        }
    }

    public static final Creator<SupplierDto> CREATOR = new Creator<SupplierDto>() {
        @Override
        public SupplierDto createFromParcel(Parcel in) {
            return new SupplierDto(in);
        }

        @Override
        public SupplierDto[] newArray(int size) {
            return new SupplierDto[size];
        }
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    public void setPrimaryPhoneNumber(String primaryPhoneNumber) {
        this.primaryPhoneNumber = primaryPhoneNumber;
    }

    public String getSecondaryPhoneNumber() {
        return secondaryPhoneNumber;
    }

    public void setSecondaryPhoneNumber(String secondaryPhoneNumber) {
        this.secondaryPhoneNumber = secondaryPhoneNumber;
    }

    public Integer getSupplierGroupId() {
        return supplierGroupId;
    }

    public void setSupplierGroupId(Integer supplierGroupId) {
        this.supplierGroupId = supplierGroupId;
    }

    public Integer getSupplierTypeId() {
        return supplierTypeId;
    }

    public void setSupplierTypeId(Integer supplierTypeId) {
        this.supplierTypeId = supplierTypeId;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(supplierName);
        if (supplierId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(supplierId);
        }
        dest.writeString(primaryPhoneNumber);
        dest.writeString(secondaryPhoneNumber);
        if (supplierGroupId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(supplierGroupId);
        }
        if (supplierTypeId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(supplierTypeId);
        }
        if (countryId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(countryId);
        }
        if (productId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(productId);
        }
    }
}
