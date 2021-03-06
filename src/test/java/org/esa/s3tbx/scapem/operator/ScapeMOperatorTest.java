/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.s3tbx.scapem.operator;

import org.esa.s3tbx.processor.rad2refl.Rad2ReflOp;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author muhammad.bc.
 */
public class ScapeMOperatorTest {
    final ScapeMOp.Spi scapeMOp = new ScapeMOp.Spi();
    final Rad2ReflOp.Spi rad2Ref = new Rad2ReflOp.Spi();
    final ScapeMSmoothSimpleKernelOp.Spi simpleKernelOp = new ScapeMSmoothSimpleKernelOp.Spi();

    @Before
    public void setUp() throws Exception {
        GPF.getDefaultInstance().getOperatorSpiRegistry().addOperatorSpi(scapeMOp);
        GPF.getDefaultInstance().getOperatorSpiRegistry().addOperatorSpi(rad2Ref);
        GPF.getDefaultInstance().getOperatorSpiRegistry().addOperatorSpi(simpleKernelOp);
    }

    @After
    public void tearDown() throws Exception {
        GPF.getDefaultInstance().getOperatorSpiRegistry().removeOperatorSpi(scapeMOp);
        GPF.getDefaultInstance().getOperatorSpiRegistry().removeOperatorSpi(rad2Ref);
        GPF.getDefaultInstance().getOperatorSpiRegistry().removeOperatorSpi(simpleKernelOp);
    }

    @Test
    public void testScapeMOperator() throws Exception {
        final Product product = prepareTestProduct();
        final Band cloud_classif_flags = product.getBand("cloud_classif_flags");
        final Band refl_1 = product.getBand("refl_1");
        final Band refl_5 = product.getBand("refl_5");
        final Band aot_550 = product.getBand("AOT_550");

        assertTrue(product.containsBand("refl_1"));
        assertTrue(product.containsBand("refl_5"));
        assertTrue(product.containsBand("refl_13"));
        assertTrue(product.containsBand("AOT_550"));
        assertTrue(product.containsBand("cloud_classif_flags"));

        assertEquals(0.0, cloud_classif_flags.getSpectralWavelength(), 1e-8);
        assertEquals(2, cloud_classif_flags.getDataElemSize());
        assertEquals(176, cloud_classif_flags.getRasterWidth());
        assertEquals(162, cloud_classif_flags.getRasterHeight());
        assertEquals(4.0, cloud_classif_flags.getSampleFloat(160, 0), 1e-8);
        assertEquals(4.0, cloud_classif_flags.getSampleFloat(160, 80), 1e-8);
        assertEquals(6.0, cloud_classif_flags.getSampleFloat(50, 50), 1e-8);
        assertEquals(0.0, cloud_classif_flags.getSampleFloat(100, 40), 1e-8);


        assertEquals(412.6910095214844, refl_1.getSpectralWavelength(), 1e-8);
        assertEquals(4, refl_1.getDataElemSize());
        assertEquals(176, refl_1.getRasterWidth());
        assertEquals(162, refl_1.getRasterHeight());
        assertEquals(0.07573655992746353, refl_1.getSampleFloat(160, 0), 1e-8);
        assertEquals(0.09557099640369415, refl_1.getSampleFloat(160, 80), 1e-8);
        assertEquals(0.07911927998065948, refl_1.getSampleFloat(120, 50), 1e-8);
        assertEquals(0.06481017917394638, refl_1.getSampleFloat(100, 40), 1e-8);


        assertEquals(559.6940307617188, refl_5.getSpectralWavelength(), 1e-8);
        assertEquals(4, refl_5.getDataElemSize());
        assertEquals(176, refl_5.getRasterWidth());
        assertEquals(162, refl_5.getRasterHeight());
        assertEquals(0.13486453890800476, refl_5.getSampleFloat(160, 0), 1e-8);
        assertEquals(0.16821929812431335, refl_5.getSampleFloat(160, 80), 1e-8);
        assertEquals(0.13388971984386444, refl_5.getSampleFloat(120, 50), 1e-8);
        assertEquals(0.10810120403766632, refl_5.getSampleFloat(100, 40), 1e-8);


        assertEquals(0.0, aot_550.getSpectralWavelength(), 1e-8);
        assertEquals(4, aot_550.getDataElemSize());
        assertEquals(176, aot_550.getRasterWidth());
        assertEquals(162, aot_550.getRasterHeight());
        assertEquals(0.6594650149345398, aot_550.getSampleFloat(160, 0), 1e-8);
        assertEquals(0.6594650149345398, aot_550.getSampleFloat(160, 80), 1e-8);
        assertEquals(0.6593074798583984, aot_550.getSampleFloat(50, 50), 1e-8);
        assertEquals(0.6594650149345398, aot_550.getSampleFloat(100, 40), 1e-8);
    }

    private Product prepareTestProduct() throws IOException {
        final String filePath = ScapeMOperatorTest.class.getResource("source_product.dim").getFile();
        final Product sourceProduct = ProductIO.readProduct(filePath);
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("reflectance_water_threshold", "0.08");
        parameters.put("thicknessOfCoast", "20");
        parameters.put("minimumOceanSize", "1600");
        parameters.put("calculateLakes", false);
        parameters.put("outputReflBand2", false);
        parameters.put("useDEM", false);
        parameters.put("skipGapFilling", false);
        parameters.put("skipVisibilitySmoothing", false);
        parameters.put("outputRhoToa", false);

        return GPF.createProduct("snap.scapeM", parameters, sourceProduct);
    }
}
