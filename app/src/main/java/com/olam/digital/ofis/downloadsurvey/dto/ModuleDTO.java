package com.olam.digital.ofis.downloadsurvey.dto;

import java.io.Serializable;

// Pojo class for village details
public class ModuleDTO implements Serializable {

    private String module_id;
    private String moduleassignment_id;
    private String moduleassignment_type;
    private String name;
    private String name_fr;
    private String name_es;
    private String name_id;
    private String name_pt;
    private String name_tr;
    private String name_lo;
    private String name_vi;
    private String name_th;

    public String getName_fr() {
        return name_fr;
    }

    public void setName_fr(String name_fr) {
        this.name_fr = name_fr;
    }

    public String getName_es() {
        return name_es;
    }

    public void setName_es(String name_es) {
        this.name_es = name_es;
    }

    public String getName_id() {
        return name_id;
    }

    public void setName_id(String name_id) {
        this.name_id = name_id;
    }

    public String getName_pt() {
        return name_pt;
    }

    public void setName_pt(String name_pt) {
        this.name_pt = name_pt;
    }

    public String getName_tr() {
        return name_tr;
    }

    public void setName_tr(String name_tr) {
        this.name_tr = name_tr;
    }

    public String getName_lo() {
        return name_lo;
    }

    public void setName_lo(String name_lo) {
        this.name_lo = name_lo;
    }

    public String getName_vi() {
        return name_vi;
    }

    public void setName_vi(String name_vi) {
        this.name_vi = name_vi;
    }

    public String getName_th() {
        return name_th;
    }

    public void setName_th(String name_th) {
        this.name_th = name_th;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule_id() {
        return module_id;
    }

    public void setModule_id(String module_id) {
        this.module_id = module_id;
    }

    public String getModuleassignment_id() {
        return moduleassignment_id;
    }

    public void setModuleassignment_id(String moduleassignment_id) {
        this.moduleassignment_id = moduleassignment_id;
    }

    public String getModuleassignment_type() {
        return moduleassignment_type;
    }

    public void setModuleassignment_type(String moduleassignment_type) {
        this.moduleassignment_type = moduleassignment_type;
    }
}
