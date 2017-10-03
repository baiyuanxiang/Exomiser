/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.autoconfigure;

import org.junit.Test;
import org.mockito.Mockito;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PrioritiserAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    public void phenixDirectoryDefaultNameIsDefinedRelativeToDataPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        Path phenixDataDirectory = (Path) this.context.getBean("phenixDataDirectory");
        assertThat(phenixDataDirectory.getFileName(), equalTo(Paths.get("phenix")));
        assertThat(phenixDataDirectory.getParent(), equalTo(TEST_DATA));
    }

    @Test
    public void phenixDirectoryIsDefinedRelativeToDataPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.phenixDataDir=wibble");
        Path phenixDataDirectory = (Path) this.context.getBean("phenixDataDirectory");
        assertThat(phenixDataDirectory.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(phenixDataDirectory.getParent(), equalTo(TEST_DATA));
    }


    @Test
    public void hpoFileDefaultIsDefinedRelativeToPhenixPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        Path path = (Path) this.context.getBean("hpoOboFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("hp.obo")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoFileIsDefinedRelativeToPhenixPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hpoFileName=wibble");
        Path path = (Path) this.context.getBean("hpoOboFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoFileBeanCanBeOverridden() {
        load(UserConfiguration.class, TEST_DATA_ENV, "exomiser.hpoFileName=wibble");
        Path path = (Path) this.context.getBean("hpoOboFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("hpo.obo")));
        assertThat(path.getParent(), equalTo((Paths.get("/another/data/dir"))));
    }

    @Test
    public void hpoAnnotationFileDefaultIsDefinedRelativeToPhenixPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        Path path = (Path) this.context.getBean("hpoAnnotationFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoAnnotationFileIsDefinedRelativeToDataPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hpoAnnotationFile=wibble");
        Path path = (Path) this.context.getBean("hpoAnnotationFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoAnnotationFileBeanCanBeOverridden() {
        load(UserConfiguration.class, TEST_DATA_ENV, "exomiser.hpoAnnotationFile=wibble");
        Path path = (Path) this.context.getBean("hpoAnnotationFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("hpo.annotations")));
        assertThat(path.getParent(), equalTo((Paths.get("/another/data/dir"))));
    }

    @Test(expected = Exception.class)
    public void randomWalkMatrixDefault() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        DataMatrix dataMatrix = (DataMatrix) context.getBean("randomWalkMatrix");
    }

    @Test
    public void randomWalkMatrixCanBeOverriden() {
        load(UserConfiguration.class, TEST_DATA_ENV, "exomiser.randomWalkFileName=wibble", "exomiser.randomWalkIndexFileName=wibbleIndex");
        DataMatrix dataMatrix = (DataMatrix) context.getBean("randomWalkMatrix");
        assertThat(dataMatrix, not(nullValue()));
    }

    @Configuration
    @ImportAutoConfiguration(PrioritiserAutoConfiguration.class)
    protected static class EmptyConfiguration {
        /*
         * Mock this otherwise we'll try connecting to a non-existent database.
         */
        @Bean
        public DataSource dataSource() {
            return Mockito.mock(DataSource.class);
        }

    }

    @Configuration
    @Import({DataDirectoryAutoConfiguration.class, EmptyConfiguration.class})
    protected static class UserConfiguration {

        @Bean
        public Path phenixDataDirectory() {
            return Paths.get("/another/data/dir/");
        }

        @Bean
        public Path hpoOboFilePath() {
            return Paths.get("/another/data/dir/hpo.obo");
        }

        @Bean
        public Path hpoAnnotationFilePath() {
            return Paths.get("/another/data/dir/hpo.annotations");
        }

        @Bean
        public DataMatrix randomWalkMatrix() {
            return Mockito.mock(DataMatrix.class);
        }
    }
}