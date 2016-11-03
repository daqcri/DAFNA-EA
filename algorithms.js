var algorithms = [
  {name: "Cosine", description: "Cosine is a heuristic approach for estimating a value confidence and source trustworthiness, based on the cosine similarity measure proposed by A. Galland, S. Abiteboul, A. Marian, and P. Senellart. Corroborating Information from Disagreeing Views. In WSDM, pp. 131–140, 2010.", params: [
    {name: 'Initial Value Confidence', value: 1, dataType: 'double', min: 0, max: 1.0, desc: "Initialization of value confidence for all properties values."},
    {name: 'Prediction constant', value: 0.2, dataType: 'double', min: 0, max: 1, desc: 'Constant that gives more weight to predictable views (e.g., sources with consistently often correct claims or consistently often wrong claims).'},
  ]},
  {name: "2-Estimates", description: "2-Estimates is a probabilistic model built over the heuristic model, Cosine and proposed by A. Galland, S. Abiteboul, A. Marian, and P. Senellart. Corroborating Information from Disagreeing Views. In WSDM, pp. 131–140, 2010.", params: [
    {name: 'Normalization Factor', value: 0.5, dataType: 'double', min: 0, max: 1, desc: "The weight used for normalizing sources' trustworthiness and values' confidence"},
  ]},
  {name: "3-Estimates", description: "3-Estimates extends 2-Estimates proposed by A. Galland, S. Abiteboul, A. Marian, and P. Senellart. Corroborating Information from Disagreeing Views. In WSDM, pp. 131–140, 2010.", params: [
    {name: 'Initial Error Factor', value: 0.4, dataType: 'double' , min: 0, max: 1.0, desc: "Initialization of value's error factor for all properties values."},
    {name: 'Normalization Factor', value: 0.5, dataType: 'double', min: 0, max: 1, desc: "The constant used for normalizing source truthworthiness and value confidence"},
  ]},
  {name: "Depen", description: "First Bayesian truth discovery model that takes into cosideration the copying relationship between sources, proposed by X. L. Dong, L. Berti-Equille, and D. Srivastava. Integrating conflicting data: The role of source dependence. PVLDB, 2(1):550–561, 2009.", params: [
    {name: 'Dependence priori probability', value: 0.2, dataType: 'double', min: 0, max: 0.5, desc: 'The a priori probability that two data sources are dependent.'},
    {name: 'Copied value probability', value: 0, dataType: 'double', min: 0, max: 1, desc: 'The probability that a value provided by a copier is copied.'},
    {name: 'Number of false values', value: 100, dataType: 'int', min: 1, desc: 'The number of false values in the underlying domain for each object.'},
    {name: 'Similarity Constant', hidden: true, value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'The similarity betwen values is weighted for the value confidence computation.'},
    {name: 'considerSimilarity', hidden: true, value: false, dataType: 'boolean', desc: 'False for Depen: the model does not take into account value similarity.'},
    {name: 'considerSourcesAccuracy', hidden: true, value: false, dataType: 'boolean', desc: 'False for Depen: the model does not take into account the accuracy of the sources.'},
    {name: 'considerDependence', hidden: true, value: true, dataType: 'boolean', desc: 'True for Depen: the model takes into account the dependence between sources.'},
    {name: 'orderSrcByDependence', hidden: true, value: false, dataType: 'boolean', desc: 'If true, Depen model is based on source dependence ordering, otherwise on lexicographic ordering.'},
  ]},
  {name: "Accu", description: "Accu extends Depen model and considers the source accuracy in addition to the source dependence, proposed by X. L. Dong, L. Berti-Equille, and D. Srivastava. Integrating conflicting data: The role of source dependence. PVLDB, 2(1):550–561, 2009.", params: [
    {name: 'Dependence a priori probability', value: 0.2, dataType: 'double', min: 0, max: 0.5, desc: 'The a priori probability that two data sources are dependent.'},
    {name: 'Copied value probability', value: 0, dataType: 'double', min: 0, max: 1, desc: 'The probability that a value provided by a copier is copied.'},
    {name: 'Number of false values', value: 100, dataType: 'int', min: 1, desc: 'The number of false values in the underlying domain for each object.'},
    {name: 'Similarity Constant', hidden: true, value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'The similarity betwen values is weighted for the value confidence computation.'},
    {name: 'considerSimilarity', hidden: true, value: false, dataType: 'boolean', desc: 'False for Accu: the model does not take into account value similarity.'},
    {name: 'considerSourcesAccuracy', hidden: true, value: true, dataType: 'boolean', desc: 'True for Accu: the model takes into account the accuracy of the sources.'},
    {name: 'considerDependency', hidden: true, value: true, dataType: 'boolean', desc: 'True for Accu: the model takes into account the dependence between sources.'},
    {name: 'orderSrcByDependence', hidden: true, value: false, dataType: 'boolean', desc: 'If true, Accu model is based on source dependence ordering, otherwise on lexicographic ordering.'},
  ]},
  {name: "AccuSim", description: "Accusim extends Accu model and considers the value similarity, proposed by X. L. Dong, L. Berti-Equille, and D. Srivastava. Integrating conflicting data: The role of source dependence. PVLDB, 2(1):550–561, 2009.", params: [
   {name: 'Dependence a priori probability', value: 0.2, dataType: 'double', min: 0, max: 0.5, desc: 'The a priori probability that two data sources are dependent.'},
    {name: 'Copied value probability', value: 0, dataType: 'double', min: 0, max: 1, desc: 'The probability that a value provided by a copier is copied.'},
    {name: 'Number of false values', value: 100, dataType: 'int', min: 1, desc: 'The number of false values in the underlying domain for each object.'},
    {name: 'Similarity Constant', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'The similarity betwen values is weighted for the value confidence computation.'},
    {name: 'considerSimilarity', hidden: true, value: true, dataType: 'boolean', desc: 'True for AccuSim: the model takes into account value similarity.'},
    {name: 'considerSourcesAccuracy', hidden: true, value: true, dataType: 'boolean', desc: 'True for AccuSim: the model takes into account the accuracy of the sources.'},
    {name: 'considerDependency', hidden: true, value: true, dataType: 'boolean', desc: 'True for Accusim: the model takes into account the dependence between sources.'},
    {name: 'orderSrcByDependence', hidden: true, value: false, dataType: 'boolean', desc: 'If true, AccuSim model is based on source dependence ordering, otherwise on lexicographic ordering.'},
  ]},
  {name: "AccuNoDep", description: "AccuNoDep extends Accu model while assuming all sources are independent, proposed by X. L. Dong, L. Berti-Equille, and D. Srivastava. Integrating conflicting data: The role of source dependence. PVLDB, 2(1):550–561, 2009.", params: [
    {name: 'Dependence a priori probability', value: 0.2, dataType: 'double', min: 0, max: 0.5, desc: 'The a priori probability that two data sources are dependent.'},
    {name: 'Copied value probability', value: 0, dataType: 'double', min: 0, max: 1, desc: 'The probability that a value provided by a copier is copied.'},
    {name: 'Number of false values', value: 100, dataType: 'int', min: 1, desc: 'The number of false values in the underlying domain for each object.'},
    {name: 'Similarity Constant', hidden: true, value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'The similarity betwen values is weighted for the value confidence computation.'},
    {name: 'considerSimilarity', hidden: true, value: false, dataType: 'boolean', desc: 'False for AccuNoDep: the model does not take into account value similarity.'},
    {name: 'considerSourcesAccuracy', hidden: true, value: true, dataType: 'boolean', desc: 'True for AccuNoDep: the model takes into account the accuracy of the sources.'},
    {name: 'considerDependency', hidden: true, value: false, dataType: 'boolean', desc: 'False for AccuNoDep: the model considers that all sources are independent.'},
    {name: 'orderSrcByDependence', hidden: true, value: false, dataType: 'boolean', desc: 'If true, AccuNoDep model is based on source dependence ordering, otherwise on lexicographic ordering.'},
  ]},
  {name: "TruthFinder", description: "TruthFinder, proposed by X. Yin, J. Han, and P. S. Yu. Truth Discovery with Multiple Conflicting Information Providers on the Web. TKDE, 20(6):796–808, 2008, is a Bayesian model that iteratively computes and updates the trustworthiness of a source as a function of the belief in its claims, and then the belief score of each claim as a function of the trustworthiness of the sources asserting it.", params: [
    {name: 'Similarity Constant', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'The similarity betwen values is weighted for the value confidence computation.'},
    {name: 'Dampening Factor', value: 0.1, dataType: 'double', min: 0, max: 1, desc: 'The damping factor compensates the effect when sources with similar values are actually dependent.'},
  ]},
  {name: "SimpleLCA", description: "SimpleLCA (Latent Credibility Analysis) is a probabilistic model proposed by J. Pasternack and D. Roth. Latent Credibility Analysis. In WWW, pp. 1009–1020, 2013. Each source has a probability of being honest, and all sources are considered independent.", params: [
    {name: 'Prior truth probability', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'Prior probability of a claim being the true claim.'},
  ]},
  {name: "GuessLCA", description: "GuessLCA extends SimpleLCA by adding a probability of a source guessing the true value while being honest.", params: [
    {name: 'Prior truth probability', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'Prior probability of a claim being the true claim.'},
  ]},
  {name: "MLE", description: "MLE (Maximum Likelihood Estimation) proposed by D. Wang, L. M. Kaplan, H. K. Le, and T. F. Abdelzaher. On Truth Discovery in Social Sensing: a Maximum Likelihood Estimation Approach. In IPSN, pp. 233–244, 2012, is based on the Expectation Maximization (EM) algorithm to quantify the reliability of sources and the correctness of their observations. It only deals with boolean positive attributes observations.", params: [
    {name: 'Prior truth probability', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'Overall prior probability of the claims being the true claims.'},
    {name: 'r', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'The probability that a source provides a value for all data items.'},
  ], multi: true},
  {name: "LTM", description: "LTM (Latent Truth Model) proposed by B. Zhao, B. I. P. Rubinstein, J. Gemmell, and J. Han. A Bayesian Approach to Discovering Truth from Conflicting Sources for Data Integration. PVLDB, 5(6):550–561, 2012, uses Bayesian Networks for estimating the truth. LTM iterates over a fixed number of iterations, and compute the value confidence using Collapsed Gibbs Sampling process.", params: [
    {name: 'Prior truth probability', value: 0.5, dataType: 'double', min: 0.0, max: 1.0, desc: 'Prior probability of how likely each claim is to be true.'},
    {name: 'Prior falsehood probability', value: 0.5, dataType: 'double', min: 0.0, max: 1.0, desc: 'Prior probability of how likely each claim is to be false.'},
    {name: 'Prior true negative count', value: 0.1, dataType: 'double', min: 0.0, max: 1.0, desc: 'Prior number of true negatives for the sources.'},
    {name: 'Prior false positive count', value: 0.9, dataType: 'double', min: 0.0, max: 1.0, desc: 'Prior number of false positives for the sources.'},
    {name: 'Prior false negative count', value: 0.1, dataType: 'double', min: 0.0, max: 1.0, desc: 'Prior number of false negatives for the sources.'},
    {name: 'Prior true positive count', value: 0.9, dataType: 'double', min: 0.0, max: 1.0, desc: 'Prior number of true positives for the sources.'},
    {name: 'Number Of Iterations', value: 500, dataType: 'int', min: 1, max: 10000, desc: 'Number of Iterations.'},
    {name: 'Burn-in', value: 100, dataType: 'int', min: 0, max: 1000, desc: 'Collapsed Gibbs Sampling burn-in period (i.e., the number of discarded first set of iterations). Must be significantly less than the number of iterations.'},
    {name: 'Thinning', value: 9, dataType: 'int', min: 0, max: 1000, desc: 'Collapsed Gibbs Sampling thinning parameter (i.e., the number of iterations to be skipped every time before considering the resul of a selected iteration). Must be significantly less than the number of iterations.'},
  ], multi: true},
  {name: "General Parameters", general: true, description: "Parameters needed for all Truth Discovery Algorithms", params: [
    {name: 'Convergence test threshold', step: 0.001, value: 0.001, dataType: 'double', min: 0, max: 0.1, desc: 'The difference of source truthworthiness cosine similarity between two successive iterations should be less than the user-defined threshold.'},
    {name: 'Initial sources truthworthiness', value: 0.8, dataType: 'double' , min: 0, max: 1.0, desc: 'Initialization of all sources truthworthiness.'},
    {name: 'Initial Value Confidence', hidden: true, value: 1, dataType: 'double', min: 0, max: 1.0, desc: "Initialization of value confidence for all properties values."},
    {name: 'Initial Error Factor', hidden: true, value: 0.4, dataType: 'double' , min: 0, max: 1.0, desc: "Initialization of error factor for all properties values."},
  ]},
  {name: "Combiner", description: "The combiner of selected truth discovery algorithms. It is automatically selected if you select more than 1 algorithm and 1 ground truth dataset. It is based on Bayes combination from P. Domingos and M. Pazzani. On the optimality of the simple Bayesian classifier under zero-one loss. Machine Learning, 29:103–130, 1997.", params: [
  ], combiner: true}
]
