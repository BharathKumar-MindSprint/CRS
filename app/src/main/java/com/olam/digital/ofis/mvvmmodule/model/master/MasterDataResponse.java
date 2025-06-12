package com.olam.digital.ofis.mvvmmodule.model.master;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.*;

import java.util.ArrayList;

public class MasterDataResponse {

        public ArrayList<Places> places;
        public ArrayList<Districts> districts;
        public ArrayList<Regions> regions;
        public ArrayList<Country> countries;
        public ArrayList<FarmerGroup> farmergroups;
        public ArrayList<Sections> sections;
        public ArrayList<Products> products;
        public ArrayList<SubDistricts> subdistricts;
        public ArrayList<Certifications> certifications;
        public ArrayList<Programme> programme;
        public ArrayList<Partners> partners;
        public String enumeratorName;

        public String getEnumeratorName() {
                return enumeratorName;
        }

        public void setEnumeratorName(String enumeratorName) {
                this.enumeratorName = enumeratorName;
        }

        public ArrayList<Places> getPlaces() {
                return places;
        }

        public void setPlaces(ArrayList<Places> places) {
                this.places = places;
        }

        public ArrayList<Districts> getDistricts() {
                return districts;
        }

        public void setDistricts(ArrayList<Districts> districts) {
                this.districts = districts;
        }

        public ArrayList<Regions> getRegions() {
                return regions;
        }

        public void setRegions(ArrayList<Regions> regions) {
                this.regions = regions;
        }

        public ArrayList<Country> getCountries() {
                return countries;
        }

        public void setCountries(ArrayList<Country> countries) {
                this.countries = countries;
        }

        public ArrayList<FarmerGroup> getFarmergroups() {
                return farmergroups;
        }

        public void setFarmergroups(ArrayList<FarmerGroup> farmergroups) {
                this.farmergroups = farmergroups;
        }

        public ArrayList<Sections> getSections() {
                return sections;
        }

        public void setSections(ArrayList<Sections> sections) {
                this.sections = sections;
        }

        public ArrayList<Products> getProducts() {
                return products;
        }

        public void setProducts(ArrayList<Products> products) {
                this.products = products;
        }

        public ArrayList<SubDistricts> getSubdistricts() {
                return subdistricts;
        }

        public void setSubdistricts(ArrayList<SubDistricts> subdistricts) {
                this.subdistricts = subdistricts;
        }
        public ArrayList<Certifications> getCertifications() {
                return certifications;
        }

        public void setCertifications(ArrayList<Certifications> certifications) {
                this.certifications = certifications;
        }

        public ArrayList<Programme> getProgramme() {
                return programme;
        }

        public void setProgramme(ArrayList<Programme> programme) {
                this.programme = programme;
        }

        public ArrayList<Partners> getPartners() {
                return partners;
        }

        public void setPartners(ArrayList<Partners> partners) {
                this.partners = partners;
        }

}
