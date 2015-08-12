package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.FilterStatus;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.MockPrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassOnlyAnalysisRunnerTest extends AnalysisRunnerTestBase {

    private PassOnlyAnalysisRunner instance;
    
    @Before
    public void setUp() {
        instance = new PassOnlyAnalysisRunner(sampleDataFactory, stubDataService);
    }

    @Test
    public void testRunAnalysis_NoFiltersNoPrioritisers() {
        Analysis analysis = makeAnalysis(vcfPath, new AnalysisStep[]{});
        
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));
        for (Gene gene : sampleData.getGenes()) {
            assertThat(gene.passedFilters(), is(true));
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                assertThat(variantEvaluation.getFilterStatus(), equalTo(FilterStatus.UNFILTERED));
            }
        }
    }

    @Test
    public void testRunAnalysis_VariantFilterOnly() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));

        Gene passedGene = sampleData.getGenes().get(0);
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getVariantEvaluations().size(), equalTo(1));
    }

    @Test
    public void testRunAnalysis_PrioritiserAndPriorityScoreFilterOnly() {
        Float desiredPrioritiserScore = 0.9f;
        Map<String, Float> geneSymbolPrioritiserScores = new HashMap<>();
        geneSymbolPrioritiserScores.put("RBM8A", desiredPrioritiserScore);

        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1f);

        Analysis analysis = makeAnalysis(vcfPath, prioritiser, priorityScoreFilter);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getPriorityScore(), equalTo(desiredPrioritiserScore));
    }

    @Test
    public void testRunAnalysis_PrioritiserPriorityScoreFilterVariantFilter() {
        Float desiredPrioritiserScore = 0.9f;
        Map<String, Float> geneSymbolPrioritiserScores = new HashMap<>();
        geneSymbolPrioritiserScores.put("RBM8A", desiredPrioritiserScore);

        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1f);
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Analysis analysis = makeAnalysis(vcfPath, prioritiser, priorityScoreFilter, intervalFilter);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getPriorityScore(), equalTo(desiredPrioritiserScore));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));

        VariantEvaluation passedVariantEvaluation = passedGene.getVariantEvaluations().get(0);
        assertThat(passedVariantEvaluation.getChromosome(), equalTo(1));
        assertThat(passedVariantEvaluation.getPosition(), equalTo(145508800));
        assertThat(passedVariantEvaluation.getGeneSymbol(), equalTo(passedGene.getGeneSymbol()));
    }

}
