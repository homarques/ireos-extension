
#Generate the difficulty plots and the difficulty measures of the datasets
runDifficultyPlot <- function(path, dataset){
	bylabel  = read.table(paste(path, "/results/", dataset, "/data", sep = "" ))
	measures = read.table(paste(path, "/evaluation/", dataset, "/data", sep = "" ), header = T, sep = ",")

	abbrev = list(KNN = "A", KNNW = "B", LOF = "C", SimplifiedLOF = "D", LoOP = "E", LDOF = "F", ODIN = "G", KDEOS = "H", COF = "I",
				  FastABOD = "J", LDF = "K", INFLO = "L")
	maxBins = 10
	n = which(bylabel[1,] == 1) - 1
	bestAUCs = c()
	bins = data.frame(matrix(rep(0, length(abbrev)*length(n)), nrow = length(abbrev), ncol = length(n)))
	bins2 = bins
	bins3 = bins
	binX = bins
	bin = bins
	
	for(i in 1:length(abbrev)){
		algorithm_id = which(measures[, 2] == names(abbrev[i]))
		bestAUC = ((measures[algorithm_id,])[order(measures[algorithm_id, 4], measures[algorithm_id, 5], decreasing=T), ])[1, c(2, 3)]
		dig  = floor(max(as.numeric(unlist(strsplit( as.character(bylabel[-1, 1]),"-"))[c(F,T)]))/100) + 1
		algo = paste(bestAUC$Algorithm, "-" ,formatC(bestAUC$k, digits = dig, flag = "0"), sep = "")

		scorings = bylabel[which(bylabel[, 1] == algo), -1]
		if(bestAUC$Algorithm == "DWOF" || bestAUC$Algorithm == "FastABOD" || bestAUC$Algorithm == "ODIN"){
			ranking = sort.list(sort.list(t(scorings), dec = F))
		}else{
			ranking = sort.list(sort.list(t(scorings), dec = T))
		}

		bins[i,]  = ceiling(ranking[n] / length(n))
		
		bin[i,] = ranking[n] / length(n)
		maxx = mean((1+length(ranking)-length(n)):length(ranking) / length(n))
		EX = ((length(ranking) + 1)/2)/length(n)
		binX[i,] = (bin[i,] - EX)/(maxx-EX)

		max       = ceiling(length(ranking) / length(n))
		E         = ceiling(((length(ranking)+1)/2) / length(n))
		bins2[i,] = bins[i,] / max
		bins3[i,] = (bins[i,] - E) / (max - E)
	}

	bins[bins > maxBins] = maxBins
	bins = bins[, order(colMeans(bins))]
	difficulty = mean(colMeans(bins))
	overMaxBins = mean(colMeans(bins2))
	adjBins = mean(colMeans(bins3))
	diversity = apply(bins, 2, sd)
	diversity = sqrt(sum(diversity^2)/length(diversity))
	adjX = mean(colMeans(binX))
	
	rocAUC     = c(min(measures[,  4]), max(measures[,  4]))
	avgPrec    = c(min(measures[,  5]), max(measures[,  5]))
	prec       = c(min(measures[,  6]), max(measures[,  6]))
	max_F1     = c(min(measures[,  7]), max(measures[,  7]))
	adjRocAUC  = c(min(measures[,  8]), max(measures[,  8]))
	adjAvgPrec = c(min(measures[,  9]), max(measures[,  9]))
	adjPrec    = c(min(measures[, 10]), max(measures[, 10]))
	adjF1      = c(min(measures[, 11]), max(measures[, 11]))

	ret = list()
	ret$measures = c(dataset, rocAUC, avgPrec, prec, max_F1, adjRocAUC, adjAvgPrec, adjPrec, adjF1, difficulty, diversity, overMaxBins, adjBins, adjX)
	ret$bins = bins

	ret
}

runSummarizeByDataset <- function(path, dataset, cores){
	abbrev = list(KNN = "A", KNNW = "B", LOF = "C", SimplifiedLOF = "D", LoOP = "E", LDOF = "F", ODIN = "G", KDEOS = "H", COF = "I",
				  FastABOD = "J", LDF = "K", INFLO = "L") #, DWOF = "M", LIC = "N", VOV = "O", Intrinsic = "P", IDOS = "Q", KDLOF = "R")
	maxBins = 10
	dir.create(paste(path, "/difficultyPlots/", sep = ""))
	datasets = list.files(paste(path, "/data_arff/", sep = ""))
	datasets = sub('\\.arff$', '', datasets)

	datasets = datasets[grepl(dataset, datasets)]
	
	sum_up = mclapply(datasets, function(dataset){
		print(dataset)
		runDifficultyPlot(path, dataset);
	}, mc.cores = cores)

	pdf(paste(path, "/difficultyPlots/", dataset, ".pdf", sep = ""))
	par(oma = c(0, 0, 0, 3), mar = c(4, 1, 1, 2), xpd = NA, mgp = c(2, 0, 0))
	for (data in sum_up){
		image(x = 1:length(abbrev), z = as.matrix(data$bins), axes = T, yaxt = 'n', xaxt = 'n', col = tim.colors(maxBins), zlim = c(1, maxBins), cex.lab = 1.2, xlab = data$measures[1])
		axis(1, at = 1:length(abbrev), labels = abbrev, tick = F, mgp = c(0, 0.5, 0), cex.axis = 1.1)
		image.plot(z = as.matrix(data$bins), legend.only = TRUE, col = tim.colors(maxBins), zlim = c(1, maxBins), legend.mar = 0, legend.cex = .5)
	}
	dev.off()
	
	data = data.frame(matrix(unlist(lapply(sum_up, '[', 1)), nrow = length(sum_up), byrow = T))
	colnames(data) = c("Dataset", "rocAUC", "rocAUC", "avgPrec", "avgPrec", "prec", "prec", "max_F1", "max_F1", "adjRocAUC", "adjRocAUC",
					   "adjAvgPrec", "adjAvgPrec", "adjPrec", "adjPrec", "adjF1", "adjF1", "difficulty", "diversity", "overMaxBins", "adjBins", "adjX")
	write.table(data, file = paste(path,"/difficultyPlots/", dataset, ".txt", sep = ""), row.names = F)
}

runBestDatasetsBy <- function(path, dataset, index, perc){
	literature = c("ALOI", "Glass", "Ionosphere", "KDDCup99", "Lymphography", "PenDigits", "Shuttle", "Waveform", "WBC", "WDBC", "WPBC")
	semantic = c("Annthyroid", "Arrhythmia", "Cardiotocography", "HeartDisease", "Hepatitis", "InternetAds", "PageBlocks", "Parkinson", "Pima", "SpamBase", "Stamps", "Wilt")
	
	data = read.table(paste(path, "/difficultyPlots/", dataset, ".txt", sep = ""), header = T, stringsAsFactors = F)
	if(dataset %in% semantic){
		if(!is.null(perc)){
			percs = as.numeric(unique(unlist(lapply(strsplit(data[,1], "_") , '[', 3))))
			percs = percs[!is.na(percs)] #downsampling availables
			perc = percs[which(abs(percs-perc) == min(abs(percs-perc)))] #closest one
			data = data[grepl(paste("_0", perc, sep=""), data[,1]),]
		}
	}

	data = data[order(data[, index]), c(1, index)]
	print(data[1, ])
	data[1,1]
}

runPickSolutions <- function(pathin, pathout, dataset, nos = 10, index = 4){
	#reading external measures and defining the steps of ROC AUC to be used according to the min and max values of it
	measures = read.table(paste(path, "/evaluation/", dataset, "/data", sep = "" ), header = T, sep = ",")
	
	sol = c()
	min = min(measures[, index])
	sol = rbind(sol, measures[order(measures[,index])[1],])
	while(nrow(sol) < nos){
		step = (max(measures[,index]) - min) / (nos - nrow(sol))
		x = seq(min, max(measures[,index]), step)
		measures = measures[which(measures[,index] >= x[2] ),]
		aux = measures[which(abs(measures[,index]-x[2]) == min(abs(measures[,index]-x[2]))),]
		temp = as.data.frame(table(sol[,2]))
		temp = temp[which(temp[,1] %in% aux[,2]),]
		temp = temp[order(temp[,2]),1][1]
		aux = aux[which(aux[,2] == temp),]
		sol = rbind(sol, aux[nrow(aux),])
		min = measures[which(abs(measures[,index]-x[2]) == min(abs(measures[,index]-x[2])))[1],index]
	}

	#saving the information (external measures, algorithms, k, etc) about the solution that will be used
	write.table(sol, paste(pathout, "/solutions_info/", dataset, sep=""), row.names= F)
}

runRankingScoring <- function(pathin, pathout, dataset){
	dir.create(paste(pathout, "/solutions/" , sep = ""))
	dir.create(paste(pathout, "/scorings/"  , sep = ""))
	dir.create(paste(pathout, "/solutions/" , dataset , sep = ""))
	dir.create(paste(pathout, "/scorings/"  , dataset , sep = ""))
	sol = read.table(paste(pathout, "/solutions_info/", dataset, sep = ""), header = T)
	
	bylabel  = read.table(paste(pathin, "/results/", dataset, "/data", sep = "" ))

	for(i in 1:nrow(sol))
	{
		if(max(as.numeric(unlist(strsplit( as.character(bylabel[-1,1]),"-"))[c(F,T)])) < 100){
			if(sol[i,]$k < 10){
				algo = paste(sol[i,]$Algorithm, "-0", sol[i,]$k, sep = "" )
			}else if(sol[i,]$k < 100){
				algo = paste(sol[i,]$Algorithm, "-", sol[i,]$k,  sep = ""  )
			}
		}else{
			if(sol[i,]$k < 10){
				algo = paste(sol[i,]$Algorithm, "-00", sol[i,]$k, sep = "" )
			}else if(sol[i,]$k < 100){
				algo = paste(sol[i,]$Algorithm, "-0", sol[i,]$k,  sep = ""  )
			}else if(sol[i,]$k == 100){
				algo = paste(sol[i,]$Algorithm, "-", sol[i,]$k,   sep = ""  )
			}
		}

		s = bylabel[which(bylabel[,1]==algo),-1]
		if(sol[i,]$Algorithm == "DWOF" || sol[i,]$Algorithm == "FastABOD" || sol[i,]$Algorithm == "ODIN")
			ranking = sort.list(sort.list(t(s), dec = F))
		else
			ranking = sort.list(sort.list(t(s), dec = T))
		write.table(ranking, paste(pathout, "/solutions/", dataset, "/", i , sep = ""), col.names = F, row.names = F)
		write.table(t(s), paste(pathout, "/scorings/", dataset, "/", i , sep = ""), col.names = F, row.names = F)
	}
}

runScoringNormalization <- function(path, dataset){
	infos = read.table(paste(path, "/solutions_info/", dataset, sep = ""), header = T)
	for( i in 1:nrow(infos)){
		filein = paste(path, "/scorings/", dataset, "/", i, sep = "")
		fileout = paste(path, "/weight/normalized_scores/", dataset, "/", i, sep = "")
		algorithm = infos[i,2]
		k = infos[i,3]
		data = read.table(filein)
		min = min(data)
		max = max(data[is.finite(t(data)),])
		size = nrow(data)
		system(paste("java -jar /home/henrique/ireos_extension/JavaCodes/scoring_normalization.jar ", filein, fileout, algorithm, min, max, k, size))
	}
}

runPickDatasets <- function(pathin, pathout, dataset){
	dir.create(paste(pathout, "/data/", sep = ""))
	data = read.arff(paste(pathin, "/data_arff/", dataset ,".arff", sep=""))
	data = data[,-which(colnames(data) == "id" |  colnames(data) == "outlier")]
	write.table(data, paste(pathout, "/data/", dataset , sep=""), col.names = F, row.names= F)

	dir.create(paste(pathout, "/solutions_info/", sep=""))
	runPickSolutions(pathin, pathout, dataset)

	dir.create(paste(pathout, "/solutions/", sep = ""))
	dir.create(paste(pathout, "/scorings/", sep = ""))
	runRankingScoring(pathin, pathout, dataset)

	dir.create(paste(pathout, "/weight/" , sep = ""))
	dir.create(paste(pathout, "/weight/normalized_scores/", sep = ""))
	dir.create(paste(pathout, "/weight/normalized_scores/", dataset , sep = ""))

	runScoringNormalization(pathout, dataset)
}

compileResults <- function(){
	path = "/home/henrique/ireos_extension/Datasets/Real/"
	files = list.files(paste(path, "/results/cln", sep =""))
	pdf("correlations.pdf")
	
	best = c()
	
	for(i in files){
		f = read.table(paste(path, "/results/cl1/", i, "/1", sep=""))
		x = unique(f[,2])
		pts = c()
		for (j in seq(1, nrow(f), 100)){
			y = f[j:(j+99),3]
			value = 0
			for(l in 2:length(y)){
				value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
			}
			value = value/2
			value = value/max(x)
			pts = c(pts, value)
		}

		ir1 = c()
		irn = c()
		irs11 = c()
		irs1n = c()
		irs21 = c()
		irs2n = c()
		irs31 = c()
		irs3n = c()
		irs41 = c()
		irs4n = c()
		sol = list.files(paste(path, "/solutions/", i , sep="/"))
		sol = sort(as.numeric(sol))
		for(j in sol){
			w = read.table(paste(path, "/weight/normalized_scores/", i, j, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			ireos = sum((w/sum(w)) * pts)		
			ir1 = c(ir1, ireos)

			fn = read.table(paste(path, "/results/cln/", i, "/", j, sep=""))
			x = unique(fn[,2])
			ptsn = c()
			for (j in seq(1, nrow(fn), 100)){
				y = fn[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				ptsn = c(ptsn, value)
			}

			ireosn = sum((w/sum(w)) * ptsn)		
			irn = c(irn, ireosn)
		}

		measures = read.table(paste(path, "/solutions_info/", i, sep  = ""), header = T )
		M = apply(cbind(measures[,4:7], ir1, irn), 2, as.numeric)
		colnames(M)[5] = "ext-IREOS cl=1"
		colnames(M)[6] = "ext-IREOS cl=n"
		colnames(M)[2] = "AP"
		colnames(M)[1] = "ROC AUC"
		#colnames(M)[3] = "prec@n"
		#colnames(M)[4] = "Max-F1"
		corrplot(cor(M, method="spearman"), method = "color", addCoef.col="black", title=i, mar = c(0,0,1,0))
		irs11 = cor(M[,1], M[,5], method="spearman")
		irs1n = cor(M[,1], M[,6], method="spearman")
		irs21 = cor(M[,2], M[,5], method="spearman")
		irs2n = cor(M[,2], M[,6], method="spearman")
		irs31 = cor(M[,3], M[,5], method="spearman")
		irs3n = cor(M[,3], M[,6], method="spearman")
		irs41 = cor(M[,4], M[,5], method="spearman")
		irs4n = cor(M[,4], M[,6], method="spearman")
		print("---------")
		print(cor(M[,1], M[,5], method="spearman"))
		print(cor(M[,1], M[,6], method="spearman"))
		print("---------")
		best = rbind(best, cbind(i, sort.list(ir1, dec=T)[1], sort.list(irn, dec=T)[1], M[sort.list(ir1, dec=T)[1],1], M[sort.list(irn, dec=T)[1],1], irs11, irs1n,irs21, irs2n,irs31, irs3n,irs41, irs4n))
		#best = c(best, sort.list(ir1, dec=T)[1])
		#box = c(box, M[sort.list(ir, dec=T)[1],1])
	}
	dev.off()
	best
}

	plotBoxPlot <- function(pts){
		pdf("boxplot.pdf")
		par(mar=par("mar")+c(2,0,0,0))

		#datasets = list.files("/home/henrique/ireos_extension/Datasets/Real/solutions_info/", full.names = T)
		datasets = pts[,1]
		box = c()
		for(i in datasets){
			infos = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/solutions_info/", i, sep = ""), header = T, stringsAsFactors = F)
			box = rbind(box, cbind(infos[1,1], infos[,4]))
		}
		#datasets = unlist(strsplit(datasets, "/"))[seq(9,length(datasets)*9,9)]
		datasets = unlist(lapply(strsplit(datasets, "_") , '[', 1))
		box = as.data.frame(box, stringsAsFactors = F)
		colnames(box)[1] = "datasets"
		colnames(box)[2] = "ROCAUC"
		box$ROCAUC = as.numeric(box$ROCAUC)
		boxplot(ROCAUC ~ datasets, data = box, xaxt="n", ylab = "ROC AUC", ylim = c(0,1))
	 	#text(seq(0.1, (length(datasets)+0.5), length.out = length(datasets)), par("usr")[3]-0.1, datasets, xpd=TRUE, srt=45)
	 	text(1:length(datasets), par("usr")[3]-0.15, datasets, xpd=TRUE, srt=90)
		points(1:length(datasets), as.numeric(pts[,4]), pch = 0, cex = 1.5, col = 2)
		points(1:length(datasets), as.numeric(pts[,5]), pch = 4, cex = 1.5, col = 2)
		labNames = "Solution selected by ext-IREOS "
		legend('bottomright',legend= c(as.expression(bquote(.(labNames) ~ (m[cl] ~"="~1))), as.expression(bquote(.(labNames) ~ (m[cl] ~"="~n)))), pch = c(0,4),bty ="n", pt.cex = c(1.5,1.5), col = c(2,2))
		dev.off()
	}

	plotBoxPlot(pts)

	plot_algorithms <- function(pts){
		pdf("algorithms.pdf")
		datasets = pts[,1]
		tabelona = c()
		for(i in 1:length(datasets)){
			infos = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/solutions_info/", datasets[i], sep = ""), header = T, stringsAsFactors = F)
			tabelona = rbind(tabelona, cbind(1:10, infos[,c(1,2)]))
		}
		tabelona[,2] = unlist(lapply(strsplit(tabelona[,2], "_") , '[', 1))
		colnames(tabelona)[1] = c("seq")
		tabelona$Algorithm = as.factor(tabelona$Algorithm)
		tabelona$Name <- factor(tabelona$Name)
 		tabelona$Name <- factor(tabelona$Name, levels = rev(levels(tabelona$Name)))
		p = ggplot(data = tabelona, aes(seq, Name))+geom_tile(aes(fill=factor(Algorithm)), colour = "white") + scale_fill_manual(values= c("#a6cee3","#1f78b4","#b2df8a",
			"#33a02c","#fb9a99","#e31a1c","#fdbf6f","#ff7f00","#cab2d6","#6a3d9a","#ffff99","#b15928")) + xlab("") + ylab("")
		p <- p + guides(fill=guide_legend(title="Algorithms")) +theme_bw() + theme(panel.border = element_blank(), panel.grid.major = element_blank(),
			panel.grid.minor = element_blank(), axis.line = element_blank(), axis.text.x = element_blank(), axis.ticks = element_blank())
		p
		dev.off()
	}

	plot_corr <- function(pts){
		pdf("corr.pdf")
		datasets = pts[,1]
		np = pts[,c(1,6,8,10,12)]
		ntabelona = data.frame()
		k = 1
		for(i in 1:nrow(np)){
			ntabelona[k, 1] = np[i,1]
			ntabelona[(k+1), 1] = np[i,1]
			ntabelona[(k+2), 1] = np[i,1]
			ntabelona[(k+3), 1] = np[i,1]
			ntabelona[k, 2] = "AUC ROC"
			ntabelona[(k+1), 2] = "AP"
			ntabelona[(k+2), 2] = "prec@n"
			ntabelona[(k+3), 2] = "Max-F1"
			ntabelona[k, 3] = np[i,2]
			ntabelona[(k+1), 3] = np[i,3]
			ntabelona[(k+2), 3] = np[i,4]
			ntabelona[(k+3), 3] = np[i,5]
			k = k+4
		}
		ntabelona[,1] = unlist(lapply(strsplit(ntabelona[,1], "_") , '[', 1))
		ntabelona[,3] = round(as.numeric(ntabelona[,3]),3)
		colnames(ntabelona) = c("Dataset","Measure","Value")
		ntabelona$Dataset <- factor(ntabelona$Dataset)
 		ntabelona$Dataset <- factor(ntabelona$Dataset, levels = rev(levels(ntabelona$Dataset)))
		col <- colorRampPalette(c("#67001F", "#B2182B", "#D6604D", "#F4A582",
                                "#FDDBC7", "#FFFFFF", "#D1E5F0", "#92C5DE",
                                "#4393C3", "#2166AC", "#053061"))(200)

		p = qplot(x=Measure, y=Dataset, data=ntabelona, fill=Value, geom="tile")  + geom_text(aes(Measure, Dataset, label = Value), color = "black", size = 4)

		p = p + guides(fill=guide_legend(title="Spearman")) +theme_bw() + theme(panel.border = element_blank(), panel.grid.major = element_blank(),
			panel.grid.minor = element_blank(), axis.line = element_blank(), axis.ticks = element_blank()) + xlab("") + ylab("")+ scale_fill_gradientn(colours = col, limits=c(-1, 1))
		p
		dev.off()
	}

library(ggplot2)
library(foreign)
library(fields)
library(parallel)
library(corrplot)
source("RL/Utils.R")
cores = 2
path = "/home/henrique/FullData/"
files = list.files(paste(path, "data_arff/", sep = ""))
files = sub('\\.arff$', '', files)

datasets = unique(unlist(lapply(strsplit(files, "_") , '[', 1)))
computed = list.files(paste(path, "/difficultyPlots/", sep = ""))
computed = sub('\\.txt$', '', computed)
computed = sub('\\.pdf$', '', computed)
computed = unique(computed)

for(i in datasets){
	if(!(i %in% computed)){
		runSummarizeByDataset(path, i, 2)
		runPickDatasets(path, "/home/henrique/ireos_extension/Datasets/Real", runBestDatasetsBy(path, i, 18, 5))
	}
}

solution_info(pts){
	datasets = pts[,1]
	for(i in 1:length(datasets)){
		infos = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/solutions_info/", datasets[i], sep = ""), header = T, stringsAsFactors = F)
		infos[,4] = as.numeric(infos[,4])
		print(datasets[i])
		print(infos[1,1])
		print(round(min(infos[,4]),4))
		print(round(max(infos[,4]),4))
		print(round(mean(infos[,4]),4))
		index = as.numeric(pts[i,2])
		print(round(infos[index,4],4))
		print(infos[index,2])
		index = as.numeric(pts[i,3])
		print(round(infos[index,4],4))
		print(infos[index,2])
	}

}






