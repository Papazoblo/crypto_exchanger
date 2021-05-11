package medvedev.com.service;

import lombok.RequiredArgsConstructor;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.Neuroph;
import org.neuroph.util.TransferFunctionType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class NeuralNetworkService {

    private static final int inputLayer = 35;
    private static final int hiddenLayer = 60;
    private static final int outputLayer = 3;

    private final PriceHistoryService priceHistoryService;

    public double[] run() {

        NeuralNetwork network = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, inputLayer,
                hiddenLayer, outputLayer);
        DataSet trainingSet = importFromArray(priceHistoryService.getPriceHistoryList(), inputLayer, outputLayer);
        network.setLearningRule(new MomentumBackpropagation());
        network.learn(trainingSet);
        network.calculate();
        double[] outputs = network.getOutput();

        Neuroph.getInstance().shutdown();
        return outputs;
    }

    private static DataSet importFromArray(double[] values, int inputsCount, int outputsCount) {
        DataSet trainingSet = new DataSet(inputsCount, outputsCount);
        for (int i = 0; i < values.length - inputsCount; i++) {
            ArrayList<Double> inputs = new ArrayList<>();
            for (int j = i; j < i + inputsCount; j++) {
                inputs.add(values[j]);
            }
            ArrayList<Double> outputs = new ArrayList<>();
            if (outputsCount > 0 && i + inputsCount + outputsCount <= values.length) {
                for (int j = i + inputsCount; j < i + inputsCount + outputsCount; j++) {
                    outputs.add(values[j]);
                }
                if (outputsCount > 0) {
                    trainingSet.add(new DataSetRow(inputs, outputs));
                } else {
                    trainingSet.add(new DataSetRow(inputs));
                }
            }
        }
        return trainingSet;
    }
}
