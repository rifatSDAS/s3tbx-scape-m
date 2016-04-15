package org.esa.s3tbx.scapem.algo;

import org.esa.s3tbx.idepix.core.IdepixConstants;
import org.esa.s3tbx.idepix.core.util.IdepixUtils;
import org.esa.s3tbx.idepix.operators.BasisOp;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;


/**
 * Idepix operator for pixel identification and classification with Scape-M cloud mask from L. Guanter, FUB.
 *
 * @author Olaf Danne
 */
@SuppressWarnings({"FieldCanBeLocal"})
@OperatorMetadata(alias = "idepix.scapem",
                  internal = true,  // currently hidden
        version = "2.2",
        authors = "Olaf Danne, Tonio Fincke",
        copyright = "(c) 2013 by Brockmann Consult",
        description = "Pixel identification and classification with Scape-M cloud mask from L. Guanter, FUB.")
public class FubScapeMOp extends BasisOp {

    @SourceProduct(alias = "source", label = "Name (MERIS L1b product)", description = "The source product.")
    private Product sourceProduct;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(description = "Reflectance Threshold for reflectance 12", defaultValue = "0.08")
    private float reflectance_water_threshold;

    @Parameter(description = "The thickness of the coastline in kilometers.", defaultValue = "20")
    private float thicknessOfCoast;

    @Parameter(description = "The minimal size for a water region to be acknowledged as an ocean in km².",
            defaultValue = "1600")
    private float minimumOceanSize;

    @Parameter(description = "Whether or not to calculate a lake mask", defaultValue = "true")
    private boolean calculateLakes;

    @Override
    public void initialize() throws OperatorException {
        final boolean inputProductIsValid = IdepixUtils.isValidMerisProduct(sourceProduct);
        if (!inputProductIsValid) {
            throw new OperatorException(IdepixConstants.INPUT_INCONSISTENCY_ERROR_MESSAGE);
        }
        processFubScapeM();
        renameL1bMaskNames(targetProduct);

    }

    private void processFubScapeM() {
        Operator operator = new FubScapeMClassificationOp();
        operator.setSourceProduct(sourceProduct);
        operator.setParameter("reflectance_water_threshold", reflectance_water_threshold);
        operator.setParameter("thicknessOfCoast", thicknessOfCoast);
        operator.setParameter("minimumOceanSize", minimumOceanSize);
        operator.setParameter("calculateLakes", calculateLakes);
        targetProduct = operator.getTargetProduct();
    }

    /**
     * The Service Provider Interface (SPI) for the operator.
     * It provides operator meta-data and is a factory for new operator instances.
     */
    public static class Spi extends OperatorSpi {

        public Spi() {
            super(FubScapeMOp.class);
        }
    }
}
