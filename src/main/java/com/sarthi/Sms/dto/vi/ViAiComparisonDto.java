package com.sarthi.Sms.dto.vi;

public class ViAiComparisonDto {
    
    private String railId;
    private String heatNumber;
    
    // Surface Defect Detection Comparison
    private SurfaceDefectComparison surfaceDefectComparison;
    
    // Dimensional Variation Detection Comparison  
    private DimensionalVariationComparison dimensionalVariationComparison;
    
    // OCR Comparison
    private OcrComparison ocrComparison;
    
    // Overall comparison status
    private boolean hasDiscrepancies;
    private String comparisonSummary;
    
    // Default constructor
    public ViAiComparisonDto() {}
    
    // Nested classes for detailed comparisons
    public static class SurfaceDefectComparison {
        private String visualInspectionFeedback;
        private String aiSurfaceDetection;
        private boolean matches;
        private String discrepancyDetails;
        private java.util.List<DefectComparisonDetail> defectComparisons;
        
        // Getters and Setters
        public String getVisualInspectionFeedback() {
            return visualInspectionFeedback;
        }
        
        public void setVisualInspectionFeedback(String visualInspectionFeedback) {
            this.visualInspectionFeedback = visualInspectionFeedback;
        }
        
        public String getAiSurfaceDetection() {
            return aiSurfaceDetection;
        }
        
        public void setAiSurfaceDetection(String aiSurfaceDetection) {
            this.aiSurfaceDetection = aiSurfaceDetection;
        }
        
        public boolean isMatches() {
            return matches;
        }
        
        public void setMatches(boolean matches) {
            this.matches = matches;
        }
        
        public String getDiscrepancyDetails() {
            return discrepancyDetails;
        }
        
        public void setDiscrepancyDetails(String discrepancyDetails) {
            this.discrepancyDetails = discrepancyDetails;
        }

        public java.util.List<DefectComparisonDetail> getDefectComparisons() {
            return defectComparisons;
        }

        public void setDefectComparisons(java.util.List<DefectComparisonDetail> defectComparisons) {
            this.defectComparisons = defectComparisons;
        }
    }
    
    public static class DimensionalVariationComparison {
        private String visualInspectionDimFeedback;
        private String aiDimensionalDetection;
        private boolean matches;
        private String discrepancyDetails;
        
        // Getters and Setters
        public String getVisualInspectionDimFeedback() {
            return visualInspectionDimFeedback;
        }
        
        public void setVisualInspectionDimFeedback(String visualInspectionDimFeedback) {
            this.visualInspectionDimFeedback = visualInspectionDimFeedback;
        }
        
        public String getAiDimensionalDetection() {
            return aiDimensionalDetection;
        }
        
        public void setAiDimensionalDetection(String aiDimensionalDetection) {
            this.aiDimensionalDetection = aiDimensionalDetection;
        }
        
        public boolean isMatches() {
            return matches;
        }
        
        public void setMatches(boolean matches) {
            this.matches = matches;
        }
        
        public String getDiscrepancyDetails() {
            return discrepancyDetails;
        }
        
        public void setDiscrepancyDetails(String discrepancyDetails) {
            this.discrepancyDetails = discrepancyDetails;
        }
    }
    
    public static class OcrComparison {
        private String visualInspectionHeatNumber;
        private String aiOcrHeatNumber;
        private boolean matches;
        private String discrepancyDetails;
        
        // Getters and Setters
        public String getVisualInspectionHeatNumber() {
            return visualInspectionHeatNumber;
        }
        
        public void setVisualInspectionHeatNumber(String visualInspectionHeatNumber) {
            this.visualInspectionHeatNumber = visualInspectionHeatNumber;
        }
        
        public String getAiOcrHeatNumber() {
            return aiOcrHeatNumber;
        }
        
        public void setAiOcrHeatNumber(String aiOcrHeatNumber) {
            this.aiOcrHeatNumber = aiOcrHeatNumber;
        }
        
        public boolean isMatches() {
            return matches;
        }
        
        public void setMatches(boolean matches) {
            this.matches = matches;
        }
        
        public String getDiscrepancyDetails() {
            return discrepancyDetails;
        }
        
        public void setDiscrepancyDetails(String discrepancyDetails) {
            this.discrepancyDetails = discrepancyDetails;
        }
    }
    
    // Main class getters and setters
    public String getRailId() {
        return railId;
    }
    
    public void setRailId(String railId) {
        this.railId = railId;
    }
    
    public String getHeatNumber() {
        return heatNumber;
    }
    
    public void setHeatNumber(String heatNumber) {
        this.heatNumber = heatNumber;
    }
    
    public SurfaceDefectComparison getSurfaceDefectComparison() {
        return surfaceDefectComparison;
    }
    
    public void setSurfaceDefectComparison(SurfaceDefectComparison surfaceDefectComparison) {
        this.surfaceDefectComparison = surfaceDefectComparison;
    }
    
    public DimensionalVariationComparison getDimensionalVariationComparison() {
        return dimensionalVariationComparison;
    }
    
    public void setDimensionalVariationComparison(DimensionalVariationComparison dimensionalVariationComparison) {
        this.dimensionalVariationComparison = dimensionalVariationComparison;
    }
    
    public OcrComparison getOcrComparison() {
        return ocrComparison;
    }
    
    public void setOcrComparison(OcrComparison ocrComparison) {
        this.ocrComparison = ocrComparison;
    }
    
    public boolean isHasDiscrepancies() {
        return hasDiscrepancies;
    }
    
    public void setHasDiscrepancies(boolean hasDiscrepancies) {
        this.hasDiscrepancies = hasDiscrepancies;
    }
    
    public String getComparisonSummary() {
        return comparisonSummary;
    }
    
    public void setComparisonSummary(String comparisonSummary) {
        this.comparisonSummary = comparisonSummary;
    }

    // Detailed defect comparison class
    public static class DefectComparisonDetail {
        private String defectCategory;
        private String defectType;
        private String location;
        private String position;
        private String cameraName;
        private Double distanceMeters;

        // AI data
        private String aiDefectType;
        private String aiLocation;
        private String aiPosition;
        private String aiCameraName;
        private Double aiDistanceMeters;

        // Comparison results
        private boolean defectTypeMatches;
        private boolean locationMatches;
        private boolean positionMatches;
        private boolean cameraNameMatches;
        private boolean distanceWithinTolerance;
        private String toleranceDetails;

        // Overall match status
        private boolean overallMatch;
        private String comparisonNotes;

        // Getters and Setters
        public String getDefectCategory() {
            return defectCategory;
        }

        public void setDefectCategory(String defectCategory) {
            this.defectCategory = defectCategory;
        }

        public String getDefectType() {
            return defectType;
        }

        public void setDefectType(String defectType) {
            this.defectType = defectType;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getCameraName() {
            return cameraName;
        }

        public void setCameraName(String cameraName) {
            this.cameraName = cameraName;
        }

        public Double getDistanceMeters() {
            return distanceMeters;
        }

        public void setDistanceMeters(Double distanceMeters) {
            this.distanceMeters = distanceMeters;
        }

        public String getAiDefectType() {
            return aiDefectType;
        }

        public void setAiDefectType(String aiDefectType) {
            this.aiDefectType = aiDefectType;
        }

        public String getAiLocation() {
            return aiLocation;
        }

        public void setAiLocation(String aiLocation) {
            this.aiLocation = aiLocation;
        }

        public String getAiPosition() {
            return aiPosition;
        }

        public void setAiPosition(String aiPosition) {
            this.aiPosition = aiPosition;
        }

        public String getAiCameraName() {
            return aiCameraName;
        }

        public void setAiCameraName(String aiCameraName) {
            this.aiCameraName = aiCameraName;
        }

        public Double getAiDistanceMeters() {
            return aiDistanceMeters;
        }

        public void setAiDistanceMeters(Double aiDistanceMeters) {
            this.aiDistanceMeters = aiDistanceMeters;
        }

        public boolean isDefectTypeMatches() {
            return defectTypeMatches;
        }

        public void setDefectTypeMatches(boolean defectTypeMatches) {
            this.defectTypeMatches = defectTypeMatches;
        }

        public boolean isLocationMatches() {
            return locationMatches;
        }

        public void setLocationMatches(boolean locationMatches) {
            this.locationMatches = locationMatches;
        }

        public boolean isPositionMatches() {
            return positionMatches;
        }

        public void setPositionMatches(boolean positionMatches) {
            this.positionMatches = positionMatches;
        }

        public boolean isCameraNameMatches() {
            return cameraNameMatches;
        }

        public void setCameraNameMatches(boolean cameraNameMatches) {
            this.cameraNameMatches = cameraNameMatches;
        }

        public boolean isDistanceWithinTolerance() {
            return distanceWithinTolerance;
        }

        public void setDistanceWithinTolerance(boolean distanceWithinTolerance) {
            this.distanceWithinTolerance = distanceWithinTolerance;
        }

        public String getToleranceDetails() {
            return toleranceDetails;
        }

        public void setToleranceDetails(String toleranceDetails) {
            this.toleranceDetails = toleranceDetails;
        }

        public boolean isOverallMatch() {
            return overallMatch;
        }

        public void setOverallMatch(boolean overallMatch) {
            this.overallMatch = overallMatch;
        }

        public String getComparisonNotes() {
            return comparisonNotes;
        }

        public void setComparisonNotes(String comparisonNotes) {
            this.comparisonNotes = comparisonNotes;
        }
    }
}
