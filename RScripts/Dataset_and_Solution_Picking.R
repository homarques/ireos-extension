syntheticData <- function(path = "ireos_extension/Datasets/Synthetic/", dataset = "gaussian20dim_4clusters_nr1"){
	data  = read.table(paste(path, "/synth-batch1/", dataset, ".csv", sep = "" ), stringsAsFactors=F)
	label = data[,ncol(data)]
	dist  = as.numeric(unlist(strsplit(data[, ncol(data)-2], "mahalDist="))[c(F,T)])
	data  = data[,1:(ncol(data)-4)]
	write.table(data, paste(path, "/data/", dataset, sep = "" ), col.names=F, row.names=F)

	ns = 10
	outliers = which(label == "outlier")
	inliers = which(label == "inlier")
	n = length(outliers)
	sol = round(seq(0, n, length.out = ns))
	sol = sol[-1]
	j = 1

	dir.create(paste(path, "/scorings/", dataset, sep = ""))
	write.table(dist, paste(path, "scorings", dataset, (ns-j+1), sep = "/" ), col.names=F, row.names=F)
	infos = c(ns, roc(label, dist)$auc, n/n)
	swp = sample(inliers, n)
	for(i in 1:n){
		aux = dist[outliers[i]]
		dist[outliers[i]] = dist[swp[i]]
		dist[swp[i]] = aux
		if(i == sol[j]){
			j = j + 1
			write.table(dist, paste(path, "scorings", dataset, (ns-j+1), sep = "/" ), col.names=F, row.names=F)
			infos = rbind(infos, c((ns-j+1), roc(label, dist)$auc, (n-i)/n))
		}
	}
	colnames(infos) = c("Solution", "AUCROC", "p@n")
	write.table(infos, paste(path, "solutions_info", dataset, sep = "/" ), row.names=F)

}

syntheticDatan <- function(path = "ireos_extension/Datasets/Synthetic/", dataset = "gaussian20dim_4clusters_nr1"){
	data  = read.table(paste(path, "/synth-batch1/", dataset, ".csv", sep = "" ), stringsAsFactors=F)
	label = data[,ncol(data)]
	
	outliers = which(label == "outlier")
	inliers = which(label == "inlier")
	n = round(sqrt(nrow(data)*0.05))
	
	print(dataset)
	print(n)

}

rerun <- function(path, dataset){
	data = read.table(paste(path, "/solutions_info/", dataset, sep = "" ), stringsAsFactors=F, header = T)
	for(i in 2:nrow(data)){
		if((data[i-1,2] - data[i, 2]) < 0.04){
			return(dataset)
		}
	}
}

runSyntheticScoringNormalization <- function(path, dataset){
	dir.create(paste(path, "/weight/", dataset, sep = ""))
	for( i in 1:10){
		filein = paste(path, "/scorings/", dataset, "/", i, sep = "")
		fileout = paste(path, "/weight/", dataset, "/", i, sep = "")
		data = read.table(filein)
		dim = as.numeric(substr(dataset, 9, 10))/2
		min = min(data)
		max = max(data[is.finite(t(data)),])
		size = nrow(data)
		system(paste("java -jar /home/henrique/ireos_extension/JavaCodes/gamma.jar ", filein, fileout, min, max, size, dim))
	}
}

#Generate the difficulty plots and the difficulty measures of the datasets
runDifficultyPlot <- function(path, dataset){
	bylabel  = read.table(paste(path, "/results/", dataset, "/data", sep = "" ))
	measures = read.table(paste(path, "/evaluation/", dataset, "/data", sep = "" ), header = T, sep = ",")

	abbrev = list(KNN = "A", KNNW = "B", LOF = "C", SimplifiedLOF = "D", LoOP = "E", LDOF = "F", ODIN = "G", KDEOS = "H", COF = "I",
				  FastABOD = "J", LDF = "K", INFLO = "L", GLOSH = "M")
	maxBins = 10
	n = which(bylabel[1,] == 1) - 1
	bestAUCs = c()
	bins = data.frame(matrix(rep(0, length(abbrev)*length(n)), nrow = length(abbrev), ncol = length(n)))
	bins2 = bins
	bins3 = bins
	binX = bins
	bin = bins
	
	for(i in 1:length(abbrev)){
		print(i)
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
				  FastABOD = "J", LDF = "K", INFLO = "L", GLOSH = "M") #, DWOF = "M", LIC = "N", VOV = "O", Intrinsic = "P", IDOS = "Q", KDLOF = "R")
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

runBestDatasetsBy <- function(path, dataset, index){
	#literature = c("ALOI", "Glass", "Ionosphere", "KDDCup99", "Lymphography", "PenDigits", "Shuttle", "Waveform", "WBC", "WDBC", "WPBC")
	#semantic = c("Annthyroid", "Arrhythmia", "Cardiotocography", "HeartDisease", "Hepatitis", "InternetAds", "PageBlocks", "Parkinson", "Pima", "SpamBase", "Stamps", "Wilt")
	
	data = read.table(paste(path, "/difficultyPlots/", dataset, ".txt", sep = ""), header = T, stringsAsFactors = F)
	#if(dataset %in% semantic){
	#	if(!is.null(perc)){
	#		percs = as.numeric(unique(unlist(lapply(strsplit(data[,1], "_") , '[', 3))))
	#		percs = percs[!is.na(percs)] #downsampling availables
	#		perc = percs[which(abs(percs-perc) == min(abs(percs-perc)))] #closest one
	#		data = data[grepl(paste("_0", perc, sep=""), data[,1]),]
	#	}
	#}

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
		fileout = paste(path, "/weight/normalized_scores_median/", dataset, "/", i, sep = "")
		#dir.create(paste(path, "/weight/normalized_scores_median/", dataset, sep = ""))
		algorithm = infos[i,2]
		k = infos[i,3]
		data = read.table(filein)
		min = min(data)
		max = max(data[is.finite(t(data)),])
		size = nrow(data)
		system(paste("java -jar /home/henrique/ireos_extension/JavaCodes/scoring_normalization_median.jar ", filein, fileout, algorithm, min, max, k, size))
	}
}

runPickDatasets <- function(pathin, pathout, dataset){
	dir.create(paste(pathout, "/data/", sep = ""))
	#data = read.arff(paste(pathin, "/data_arff/", dataset ,".arff", sep=""))
	#data = data[,-which(colnames(data) == "id" |  colnames(data) == "outlier")]
	#write.table(data, paste(pathout, "/data/", dataset , sep=""), col.names = F, row.names= F)

	dir.create(paste(pathout, "/solutions_info/", sep=""))
	runPickSolutions(pathin, pathout, dataset)

	dir.create(paste(pathout, "/solutions/", sep = ""))
	dir.create(paste(pathout, "/scorings/", sep = ""))
	runRankingScoring(pathin, pathout, dataset)

	dir.create(paste(pathout, "/weight/" , sep = ""))
	dir.create(paste(pathout, "/weight/normalized_scores_median/", sep = ""))
	dir.create(paste(pathout, "/weight/normalized_scores_median/", dataset , sep = ""))

	runScoringNormalization(pathout, dataset)
}

runDataConvert <- function(path, dataset){
	dir.create(paste(path, "/GM/" , sep=""))

	#converting the arff data
	data = read.table(paste(path, "/data/", dataset, sep=""))
	write.table(1/quantile(dist(data)^2, c(0.1)), paste(path, "/GM/", dataset , sep=""), col.names = F, row.names = F, quote = F)
}

wz <- function(){
	files = list.files("Compile/Real/")
	for(i in files){
		rt = 0
		tt = 0
		tz = 0
		rz = 0
		cat(unique(unlist(lapply(strsplit(i, "_") , '[', 1))))
		
		mcl = list.files(paste("Compile/Real/", i, sep=""))
		pts = list()
		pts2 = list()
		pts3 = list()
		pt = list()
		pt2 = list()
		pt3 = list()
		run = list()
		run2 = list()
		run3 = list()
		w = list()
		for(k in 1:10){
			f1 = read.table(paste("Compile/Real/", i, "/mcl1/1", sep=""))
			x = unique(f1[,2])
			tmp = c()
			temp = c()
			for (j in seq(1, nrow(f1), 100)){
				y = f1[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				tmp = c(tmp, value)
				temp = c(temp, sum(f1[j:(j+99),4]))
			}
			pts[[k]] = tmp
			run[[k]] = temp

			f2 = read.table(paste("Compile/Real/", i, mcl[2], k, sep="/"))
			x = unique(f2[,2])
			tmp = c()
			temp = c()
			for (j in seq(1, nrow(f2), 100)){
				y = f2[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				tmp = c(tmp, value)
				temp = c(temp, sum(f2[j:(j+99),4]))
			}
			pts2[[k]] = tmp
			run2[[k]] = temp

			if(i != "Parkinson_withoutdupl_norm_05_v02"){
				f3 = read.table(paste("Compile/Real/", i, mcl[3], k, sep="/"))
			}else{
				f3 = read.table(paste("Compile/Real/", i, mcl[2], k, sep="/"))
			}
			x = unique(f3[,2])
			tmp = c()
			temp = c()
			for (j in seq(1, nrow(f3), 100)){
				y = f3[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				tmp = c(tmp, value)
				temp = c(temp, sum(f3[j:(j+99),4]))
			}
			pts3[[k]] = tmp
			run3[[k]] = temp
			
			w[[k]] = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/weight/normalized_scores_median/", i, k, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			pt[[k]] = rep(1, nrow(w[[k]]))
			pt2[[k]] = rep(1, nrow(w[[k]]))
			pt3[[k]] = rep(1, nrow(w[[k]]))
			if(sum(w[[k]]) != 0){
				w[[k]] = w[[k]]/sum(w[[k]])
			}

			rt = rt + sum(as.numeric(f1[,4])) + sum(as.numeric(f2[,4])) + sum(as.numeric(f3[,4]))
			tt = tt + nrow(f1) + nrow(f2) + nrow(f3)
			rz = rz + sum(as.numeric(run[[k]][which(w[[k]]>0)])) + sum(as.numeric(run2[[k]][which(w[[k]]>0)])) + sum(as.numeric(run3[[k]][which(w[[k]]>0)]))
			tz = tz + length(which(w[[k]]>0))*300

		}
		
	
		tt = tt/30
		rt = rt/30
		tz = tz/30
		rz = rz/30
		cat(" & ", tt, " & ", sep="")
		seconds = floor((rt/(1000))%%60)
		minutes = floor((rt/(1000*60))%%60)
		hours   = floor((rt/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, sep="")
		cat(" & ", tz, " (", round((tz*100)/tt, 2) ,"\\%) & ", sep="")

		seconds = floor((rz/(1000))%%60)
		minutes = floor((rz/(1000*60))%%60)
		hours   = floor((rz/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, " (", round((rz*100)/rt, 2) ,"\\%)", sep="")
		
		#nao ultrapassa e n e ultrapassado

		tc = c()
		tp = c()
		tc2 = c()
		tp2 = c()
		tc3 = c()
		tp3 = c()
		tx = 0
		rx = 0
		for(o in 1:nrow(w[[1]])){
			for(k in 1:10){
				pt[[k]][sort.list(t(w[[k]]), dec = T)[o]] = pts[[k]][sort.list(t(w[[k]]), dec = T)[o]]
				pt2[[k]][sort.list(t(w[[k]]), dec = T)[o]] = pts2[[k]][sort.list(t(w[[k]]), dec = T)[o]]
				pt3[[k]][sort.list(t(w[[k]]), dec = T)[o]] = pts3[[k]][sort.list(t(w[[k]]), dec = T)[o]]
				tp[k] = sum(w[[k]] * pt[[k]])
				tc[k] = sum(w[[k]][sort.list(t(w[[k]]), dec = T)[1:o],] * pt[[k]][sort.list(t(w[[k]]), dec = T)[1:o]])
				tp2[k] = sum(w[[k]] * pt2[[k]])
				tc2[k] = sum(w[[k]][sort.list(t(w[[k]]), dec = T)[1:o],] * pt2[[k]][sort.list(t(w[[k]]), dec = T)[1:o]])
				tp3[k] = sum(w[[k]] * pt3[[k]])
				tc3[k] = sum(w[[k]][sort.list(t(w[[k]]), dec = T)[1:o],] * pt3[[k]][sort.list(t(w[[k]]), dec = T)[1:o]])
			}

			flgA = c()
			flgB = c()
			drop = 1:10
			for(k in drop){
				flgA[k] = T
				flgB[k] = T
				for(u in which(tc < tc[k])){
					if(tc[k] < tp[u]){ #nao e ultrapassado
						flgA[k] = F
						break;
					}
				}

				for(u in which(tc > tc[k])){
					if(tp[k] > tc[u]){ #nao ultrapassa
						flgB[k] = F
						break;
					}
				}

				if(w[[k]][sort.list(t(w[[k]]), dec = T)[o],] == 0){
					drop = drop[-which(drop == k)]
				#	print(paste(k, "Dropado"))
				}else if(flgA[k] && flgB[k]){
					drop = drop[-which(drop == k)]
					#print(paste(k, "Dropado"))
				}else{
					tx = tx + 100
					rx = rx + run[[k]][sort.list(t(w[[k]]), dec = T)[o]]
				#	print(paste(k, "somado"))
				}
			#	print(paste(k,tc[k], tp[k]))
			}

			drop = 1:10
			for(k in drop){
				flgA[k] = T
				flgB[k] = T
				for(u in which(tc2 < tc2[k])){
					if(tc2[k] < tp2[u]){ #nao e ultrapassado
						flgA[k] = F
						break;
					}
				}

				for(u in which(tc2 > tc2[k])){
					if(tp2[k] > tc2[u]){ #nao ultrapassa
						flgB[k] = F
						break;
					}
				}

				if(w[[k]][sort.list(t(w[[k]]), dec = T)[o],] == 0){
					drop = drop[-which(drop == k)]
				}else if(flgA[k] && flgB[k]){
					drop = drop[-which(drop == k)]
				}else{
					tx = tx + 100
					rx = rx + run2[[k]][sort.list(t(w[[k]]), dec = T)[o]]
				}
			}

			drop = 1:10
			for(k in drop){
				flgA[k] = T
				flgB[k] = T
				for(u in which(tc3 < tc3[k])){
					if(tc3[k] < tp3[u]){ #nao e ultrapassado
						flgA[k] = F
						break;
					}
				}

				for(u in which(tc3 > tc3[k])){
					if(tp3[k] > tc3[u]){ #nao ultrapassa
						flgB[k] = F
						break;
					}
				}


				if(w[[k]][sort.list(t(w[[k]]), dec = T)[o],] == 0){
					drop = drop[-which(drop == k)]
				}else if(flgA[k] && flgB[k]){
					drop = drop[-which(drop == k)]
				}else{
					tx = tx + 100
					rx = rx + run3[[k]][sort.list(t(w[[k]]), dec = T)[o]]
				}
			}

		}
		tx = tx/30
		rx = rx/30
		cat(" & ", tx, " (", round((tx*100)/tt, 2) ,"\\%) & ", sep="")

		seconds = floor((rx/(1000))%%60)
		minutes = floor((rx/(1000*60))%%60)
		hours   = floor((rx/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, " (", round((rx*100)/rt, 2) ,"\\%) \\\\ \n", sep="")
	}
}

exptThree <- function(){
	files = list.files("Compile/Real/")
	for(i in files){
		rt = 0
		tt = 0
		cat(unique(unlist(lapply(strsplit(i, "_") , '[', 1))))
		f1 = read.table(paste("Compile/Real/", i, "/mcl1/1", sep=""))
		x = unique(f1[,2])
		pts = c()
		for (j in seq(1, nrow(f1), 100)){
			y = f1[j:(j+99),3]
			value = 0
			for(l in 2:length(y)){
				value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
			}
			value = value/2
			value = value/max(x)
			pts = c(pts, value)
		}
		rt = sum(as.numeric(f1[,4])) *10
		tt = nrow(f1) * 10
		mcl = list.files(paste("Compile/Real/", i, sep=""))
		ir1o = c()
		irso = c()
		irno = c()
		for(k in 1:10){
			f2 = read.table(paste("Compile/Real/", i, mcl[2], k, sep="/"))
			x = unique(f2[,2])
			pts2 = c()
			for (j in seq(1, nrow(f2), 100)){
				y = f2[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts2 = c(pts2, value)
			}


			if(i != "Parkinson_withoutdupl_norm_05_v02"){
				f3 = read.table(paste("Compile/Real/", i, mcl[3], k, sep="/"))
			}else{
				f3 = read.table(paste("Compile/Real/", i, mcl[2], k, sep="/"))
			}
			x = unique(f3[,2])
			pts3 = c()
			for (j in seq(1, nrow(f3), 100)){
				y = f3[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts3 = c(pts3, value)
			}
		
			w = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/weight/normalized_scores_median/", i, k, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			if(sum(w) != 0){
				ireos = sum((w/sum(w)) * pts)
				ir1o = c(ir1o, ireos)
				ireos = sum((w/sum(w)) * pts2)
				irso = c(irso, ireos)
				ireos = sum((w/sum(w)) * pts3)
				irno = c(irno, ireos)
			}else{
				ir1o = c(ir1o, 0)
				irso = c(irso, 0)
				irno = c(irno, 0)
			}
			rt = rt + sum(as.numeric(f2[,4])) + sum(as.numeric(f3[,4]))
			tt = tt + nrow(f2) + nrow(f3)
		}
		tt = tt/30
		rt = rt/30
		cat(" & ", tt, " & ", sep="")
		seconds = floor((rt/(1000))%%60)
		minutes = floor((rt/(1000*60))%%60)
		hours   = floor((rt/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, sep="")


		f = read.table(paste("DB/expthree/", i, "/1", sep=""))
		f = f[order(f[,1], f[,2], f[,3]),]
		mcl = sort(unique(f[,1]))
		if(i == "Parkinson_withoutdupl_norm_05_v02"){
			mcl = c(mcl, mcl[2])
		}

		f1 = f[which(f[,1]==1),c(2,3,4,5)]
		runtime = 0
		total = 0

		size = max(f1[,1])
		gammaMax = max(f1[,2])
		pts = c()
		for(j in 0:size){
			ret = adaptiveQuads(0, gammaMax, 0.999, f1[which(f1[,1] == j),], T)
			#print(ret$errest)
			pts = c(pts, ret$norm)
			runtime = runtime + ret$runtime
			total = total + ret$total
		}
		runtime = runtime*10
		total = total*10
		
		ir1 = c()
		irs = c()
		irn = c()
		for(k in 1:10){
			f = read.table(paste("DB/expthree/", i, k, sep="/"))
			f = f[order(f[,1], f[,2], f[,3]),]
			f2 = f[which(f[,1] == mcl[2]),c(2,3,4,5)]
			size = max(f2[,1])
			gammaMax = max(f2[,2])
			pts2 = c()
			for(j in 0:size){
				ret = adaptiveQuads(0, gammaMax, 0.999, f2[which(f2[,1] == j),], T)
				#print(ret$errest)
				pts2 = c(pts2, ret$norm)
				runtime = runtime + ret$runtime
				total = total + ret$total
			}

			f3 = f[which(f[,1] == mcl[3]),c(2,3,4,5)]
			size = max(f3[,1])
			gammaMax = max(f3[,2])
			pts3 = c()
			for(j in 0:size){
				ret = adaptiveQuads(0, gammaMax, 0.999, f3[which(f3[,1] == j),], T)
				#print(ret$errest)
				pts3 = c(pts3, ret$norm)
				runtime = runtime + ret$runtime
				total = total + ret$total
			}

			w = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/weight/normalized_scores_median/", i, k, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			ireos = sum((w/sum(w)) * pts)
			if(sum(w) != 0){
				ireos = sum((w/sum(w)) * pts)
				ir1 = c(ir1, ireos)
				ireos = sum((w/sum(w)) * pts2)
				irs = c(irs, ireos)
				ireos = sum((w/sum(w)) * pts3)
				irn = c(irn, ireos)
			}else{
				ir1 = c(ir1, 0)
				irs = c(irs, 0)
				irn = c(irn, 0)
			}
		}
		total = total / 30
		runtime = runtime / 30

		cat(" & ", total, " (", round((total*100)/tt, 2) ,"\\%) & ", sep="")

		seconds = floor((runtime/(1000))%%60)
		minutes = floor((runtime/(1000*60))%%60)
		hours   = floor((runtime/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, " (", round((runtime*100)/rt, 2) ,"\\%) ", sep="")
		df = c(ir1o-ir1, irso-irs, irno-irn) 
		cat(" & ", round(mean(abs(df)),5), " \\\\ \n")	
	}
}

0.99 = 0.01
0.999 = 0.001
0.9999 = 0.0001


adaptiveQuads <- function(a, b, tol, gammas, flg){
	s1 = simpsonRule(a, b, gammas)
	m = (a + b) / 2;
	s21 = simpsonRule(a, m, gammas)
	s22 = simpsonRule(m, b, gammas)
	s2 = list()
	s2$sum = s21$sum + s22$sum
	s2$runtime = s21$runtime + s22$runtime
	s2$total = s21$total + s22$total
	s2$norm = (s21$sum + s22$sum)/(b-a)
	if(flg){
		errest = abs(s1$sum - s2$sum) / 7.5;
	}else{
		errest = abs(s1$sum - s2$sum) / 15;
	}

	if (errest > ((b-a)-((b-a)*tol))){
		p1 = adaptiveQuads(a, m, tol, gammas, flg)
		p2 = adaptiveQuads(m, b, tol, gammas, flg)
		p = list()
		p$sum = p1$sum + p2$sum
		p$runtime = p1$runtime + p2$runtime
		p$total = p1$total + p2$total
		p$norm = (p1$sum + p2$sum)/(b-a)
		return(p)
	}else{
		return(s2)
	}
}

simpsonRule <- function(a, b, gammas){
	g1 = gammas[which(gammas[,2] == a),]
	g2 = gammas[which(gammas[,2] == ((a + b) / 2)),]
	g3 = gammas[which(gammas[,2] == b),]

	sum = ((b - a) / 6)	* (g1[,3] + 4 * g2[,3] + g3[,3])
	runtime = g1[,4] + g2[,4] + g3[,4]
	total = 3

	ret = list()
	ret$sum = sum
	ret$runtime = runtime
	ret$total = total
	ret$norm = sum/(b-a)

	ret
}

compileResults <- function(){
		files = list.files("DB/exptwo")
		computed = list.files("/home/henrique/FullData/compile_median/")
		best = c()
		runtime = 0
		total = 0
		for(i in files){
			print(i)
			if(!(i %in% computed)){
				mtry <- try(read.table(paste("DB/exptwo/", i, "/1", sep="")), silent = TRUE)
				while(class(mtry) == "try-error"){
					line = as.numeric(strsplit(mtry[[1]], " ")[[1]][23])
					tmp = readLines(paste("DB/exptwo/", i, "/1", sep="/"))
					tmp = tmp[-line]
					write.table(tmp, col.names=F, row.names=F, file=paste("DB/exptwo/", i, "/1", sep=""), quote=F)
					mtry <- try(read.table(paste("DB/exptwo/", i, "/1", sep="")), silent = F)
				}
				f = read.table(paste("DB/exptwo/", i, "/1", sep=""))
				f = f[order(f[,1], f[,2], f[,3]),]
				mcl = sort(unique(f[,1]))
				if(i == "Parkinson_withoutdupl_norm_05_v02"){
					mcl = c(mcl, mcl[2])
				}
				f1 = f[which(f[,1]==1),c(2,3,4,5)]
				
				temp = c()
				for(l in 0:max(f1[,1])){
					fx = f1[which(f1[,1] == l),]
					fx = fx[!duplicated(fx[,2]),]
					if(nrow(fx) != 100){
						cat("ERROR: ", l,"\n")
					}
					temp = rbind(temp, fx)
				}

				f1 = temp
				total = nrow(f1)*10
				runtime = sum(as.numeric(f1[,4]))*10

				x = unique(f1[,2])
				pts = c()
				for (j in seq(1, nrow(f1), 100)){
					y = f1[j:(j+99),3]
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
				irs = c()
				path="/home/henrique/ireos_extension/Datasets/Real/"
				sol = 1:10
				for(j in sol){
					w = read.table(paste(path, "/weight/normalized_scores/", i, j, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
					ireos = sum((w/sum(w)) * pts)
					if(is.na(ireos)){
						ireos = 0
					}
					ir1 = c(ir1, ireos)

					if(length(mcl) == 3 ){
						mtry <- try(read.table(paste("DB/exptwo/", i, j, sep="/")), silent = TRUE)
						while(class(mtry) == "try-error"){
							line = as.numeric(strsplit(mtry[[1]], " ")[[1]][23])
							tmp = readLines(paste("DB/exptwo/", i, j, sep="/"))
							tmp = tmp[-line]
							write.table(tmp, col.names=F, row.names=F, file=paste("DB/exptwo/", i, j, sep="/"), quote=F)
							mtry <- try(read.table(paste("DB/exptwo/", i, j, sep="/")), silent = F)
						}

						f = read.table(paste("DB/exptwo/", i, j, sep="/"))
						f = f[order(f[,1], f[,2], f[,3]),]
						fn = f[which(f[,1]==mcl[3]),c(2,3,4,5)]
						
						temp = c()
						for(l in 0:max(fn[,1])){
							fx = fn[which(fn[,1] == l),]
							fx = fx[!duplicated(fx[,2]),]
							if(nrow(fx) != 100){
								cat("ERROR: ", l,"\n")
							}
							temp = rbind(temp, fx)
						}

						fn = temp
						total = total + nrow(fn)
						runtime = runtime + sum(as.numeric(fn[,4]))

						x = unique(fn[,2])
						ptsn = c()
						for (j in seq(1, nrow(f1), 100)){
							y = fn[j:(j+99),3]
							value = 0
							for(l in 2:length(y)){
								value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
							}
							value = value/2
							value = value/max(x)
							ptsn = c(ptsn, value)
						}
						ireos = sum((w/sum(w)) * ptsn)
						if(is.na(ireos)){
							ireos = 0
						}
						irn = c(irn, ireos)

						fs = f[which(f[,1]==mcl[2]),c(2,3,4,5)]
						
						temp = c()
						for(l in 0:max(fs[,1])){
							fx = fs[which(fs[,1] == l),]
							fx = fx[!duplicated(fx[,2]),]
							if(nrow(fx) != 100){
								cat("ERROR: ", l,"\n")
							}
							temp = rbind(temp, fx)
						}

						fs = temp
						total = total + nrow(fs)
						runtime = runtime + sum(as.numeric(fs[,4]))

						x = unique(fs[,2])
						ptss = c()
						for (j in seq(1, nrow(f1), 100)){
							y = fs[j:(j+99),3]
							value = 0
							for(l in 2:length(y)){
								value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
							}
							value = value/2
							value = value/max(x)
							ptss = c(ptss, value)
						}
						ireos = sum((w/sum(w)) * ptss)
						if(is.na(ireos)){
							ireos = 0
						}
						irs = c(irs, ireos)
					}
				}
			#	write.table(file=paste("/home/henrique/FullData/compile_median/", i, sep=""), x=t(ir1), col.names=F, row.names="ir1")
			#	write.table(file=paste("/home/henrique/FullData/compile_median/", i, sep=""), x=t(irs), col.names=F, row.names="irs", append = TRUE)
			#	write.table(file=paste("/home/henrique/FullData/compile_median/", i, sep=""), x=t(irn), col.names=F, row.names="irn", append = TRUE)
			#	write.table(file=paste("/home/henrique/FullData/compile_median/", i, sep=""), x=runtime, col.names=F, row.names="runtime", append = TRUE)
			#	write.table(file=paste("/home/henrique/FullData/compile_median/", i, sep=""), x=total, col.names=F, row.names="total", append = TRUE)
			}else{
				ir1 = t(read.table(paste("/home/henrique/FullData/compile_median/", i, sep=""), nrow=1, row.names=1))
				irs = t(read.table(paste("/home/henrique/FullData/compile_median/", i, sep=""), nrow=1, skip=1, row.names=1))
				irn = t(read.table(paste("/home/henrique/FullData/compile_median/", i, sep=""), nrow=1, skip=2, row.names=1))
				runtime = t(read.table(paste("/home/henrique/FullData/compile_median/", i, sep=""), nrow=1, skip=3, row.names=1))
				total = t(read.table(paste("/home/henrique/FullData/compile_median/", i, sep=""), nrow=1, skip=4, row.names=1))
			}
			
			seconds = floor((runtime/(1000))%%60)
			minutes = floor((runtime/(1000*60))%%60)
			hours   = floor((runtime/(1000*60*60)))
			cat(hours,":",minutes,":",seconds,"\n")
			print(total)
			runtime = runtime/30
			seconds = floor((runtime/(1000))%%60)
			minutes = floor((runtime/(1000*60))%%60)
			hours   = floor((runtime/(1000*60*60))%%24)
			cat(hours,":",minutes,":",seconds,"\n")
			print(total/30)
			#print(ir1)
			print(cor(ir1, seq(1,10), method="spearman"))
			print(cor(irs, seq(1,10), method="spearman"))
			print(cor(irn, seq(1,10), method="spearman"))
			print(sort.list(ir1, dec=T)[1])
			print(sort.list(irs, dec=T)[1])
			print(sort.list(irn, dec=T)[1])

			best = rbind(best, rbind(c(i, '1', sort.list(ir1, dec=T)[1]), c(i, 'sqrt', sort.list(irs, dec=T)[1]), c(i, 'n', sort.list(irn, dec=T)[1])))
			#box = c(box, M[sort.list(ir, dec=T)[1],1])
		}
		best
}

compileResults <- function(){
	files = list.files("DB/expfour/mcl1/10")
	best = c()
	for(i in files){
		rt = 0
		tt = 0
		cat(unique(unlist(lapply(strsplit(i, "_") , '[', 1))), " & ")
		f1 = read.table(paste("Compile/Real/", i, "/mcl1/1", sep=""))
		x = unique(f1[,2])
		pts = c()
		for (j in seq(1, nrow(f1), 100)){
			y = f1[j:(j+99),3]
			value = 0
			for(l in 2:length(y)){
				value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
			}
			value = value/2
			value = value/max(x)
			pts = c(pts, value)
		}
		rt = sum(as.numeric(f1[,4])) *10
		tt = nrow(f1) * 10
		mcl = list.files(paste("Compile/Real/", i, sep=""))
		ir1o = c()
		irso = c()
		irno = c()
		for(k in 1:10){
			f2 = read.table(paste("Compile/Real/", i, mcl[2], k, sep="/"))
			x = unique(f2[,2])
			pts2 = c()
			for (j in seq(1, nrow(f2), 100)){
				y = f2[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts2 = c(pts2, value)
			}


			if(i != "Parkinson_withoutdupl_norm_05_v02"){
				f3 = read.table(paste("Compile/Real/", i, mcl[3], k, sep="/"))
			}else{
				f3 = read.table(paste("Compile/Real/", i, mcl[2], k, sep="/"))
			}
			x = unique(f3[,2])
			pts3 = c()
			for (j in seq(1, nrow(f3), 100)){
				y = f3[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts3 = c(pts3, value)
			}
		
			w = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/weight/normalized_scores_median/", i, k, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			if(sum(w) != 0){
				ireos = sum((w/sum(w)) * pts)
				ir1o = c(ir1o, ireos)
				ireos = sum((w/sum(w)) * pts2)
				irso = c(irso, ireos)
				ireos = sum((w/sum(w)) * pts3)
				irno = c(irno, ireos)
			}else{
				ir1o = c(ir1o, 0)
				irso = c(irso, 0)
				irno = c(irno, 0)
			}
			rt = rt + sum(as.numeric(f2[,4])) + sum(as.numeric(f3[,4]))
			tt = tt + nrow(f2) + nrow(f3)
		}
		tt = tt/30
		rt = rt/30
		seconds = floor((rt/(1000))%%60)
		minutes = floor((rt/(1000*60))%%60)
		hours   = floor((rt/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, sep="")



		f1 = read.table(paste("DB/expfour/mcl1/10/", i, "/1", sep=""))
		runtime = sum(as.numeric(f1[,4]))*10

		x = unique(f1[,2])
		pts = c()
		for (j in seq(1, nrow(f1), 100)){
			y = f1[j:(j+99),3]
			value = 0
			for(l in 2:length(y)){
				value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
			}
			value = value/2
			value = value/max(x)
			pts = c(pts, value)
		}


		ir1 = c()
		irs = c()
		irn = c()
		runtime = 0

		for(k in 1:10){
			f2 = read.table(paste("DB/expfour/sqrt/10/", i, "/", k, sep=""))
			runtime = runtime + sum(as.numeric(f2[,4]))

			x = unique(f2[,2])
			pts2 = c()
			for (j in seq(1, nrow(f2), 100)){
				y = f2[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
<<<<<<< HEAD
				pts2 = c(pts2, value)
			}

			f3 = read.table(paste("DB/expfour/n/10/", i, "/", k, sep=""))
			runtime = runtime + sum(as.numeric(f3[,4]))

			x = unique(f3[,2])
			pts3 = c()
			for (j in seq(1, nrow(f3), 100)){
				y = f3[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts3 = c(pts3, value)
			}

			w = read.table(paste("ireos_extension/Datasets/Real/weight/normalized_scores_median/", i, k, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			if(sum(w) != 0){
				ireos = sum((w/sum(w)) * pts)
				ir1 = c(ir1, ireos)
				ireos = sum((w/sum(w)) * pts2)
				irs = c(irs, ireos)
				ireos = sum((w/sum(w)) * pts3)
				irn = c(irn, ireos)
			}else{
				ir1 = c(ir1, 0)
				irs = c(irs, 0)
				irn = c(irn, 0)
			}
		}
		
		df = c(ir1o[which(!is.nan(ir1))]-ir1[which(!is.nan(ir1))], irso[which(!is.nan(irs))]-irs[which(!is.nan(irs))], irno[which(!is.nan(irn))]-irn[which(!is.nan(irn))]) 
		cat(" & ", round(mean(abs(df)),5), " $\\pm$ ", round(sd(df),5)," & ", sep="")
		runtime = runtime/30
		seconds = floor((runtime/(1000))%%60)
		minutes = floor((runtime/(1000*60))%%60)
		hours   = floor((runtime/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, sep="")

		runtime = 0
		f1 = read.table(paste("DB/expfour/mcl1/250/", i, "/1", sep=""))
		runtime = sum(as.numeric(f1[,4]))*10

		x = unique(f1[,2])
		pts = c()
		for (j in seq(1, nrow(f1), 100)){
			y = f1[j:(j+99),3]
			value = 0
			for(l in 2:length(y)){
				value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
			}
			value = value/2
			value = value/max(x)
			pts = c(pts, value)
		}


		ir1 = c()
		irs = c()
		irn = c()

		for(k in 1:10){
			f2 = read.table(paste("DB/expfour/sqrt/250/", i, "/", k, sep=""))
			runtime = runtime + sum(as.numeric(f2[,4]))

			x = unique(f2[,2])
			pts2 = c()
			for (j in seq(1, nrow(f2), 100)){
				y = f2[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts2 = c(pts2, value)
			}

			f3 = read.table(paste("DB/expfour/n/250/", i, "/", k, sep=""))
			runtime = runtime + sum(as.numeric(f3[,4]))

			x = unique(f3[,2])
			pts3 = c()
			for (j in seq(1, nrow(f3), 100)){
				y = f3[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts3 = c(pts3, value)
			}

			w = read.table(paste("ireos_extension/Datasets/Real/weight/normalized_scores_median/", i, k, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			if(sum(w) != 0){
				ireos = sum((w/sum(w)) * pts)
				ir1 = c(ir1, ireos)
				ireos = sum((w/sum(w)) * pts2)
				irs = c(irs, ireos)
				ireos = sum((w/sum(w)) * pts3)
				irn = c(irn, ireos)
			}else{
				ir1 = c(ir1, 0)
				irs = c(irs, 0)
				irn = c(irn, 0)
			}
		}
		
		df = c(ir1o[which(!is.nan(ir1))]-ir1[which(!is.nan(ir1))], irso[which(!is.nan(irs))]-irs[which(!is.nan(irs))], irno[which(!is.nan(irn))]-irn[which(!is.nan(irn))]) 
		cat(" & ", round(mean(abs(df)),5), " $\\pm$ ", round(sd(df),5)," & ", sep="")
		runtime = runtime/30
		seconds = floor((runtime/(1000))%%60)
		minutes = floor((runtime/(1000*60))%%60)
		hours   = floor((runtime/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, sep="")

		runtime = 0
		f1 = read.table(paste("DB/expfour/mcl1/500/", i, "/1", sep=""))
		runtime = sum(as.numeric(f1[,4]))*10

		x = unique(f1[,2])
		pts = c()
		for (j in seq(1, nrow(f1), 100)){
			y = f1[j:(j+99),3]
			value = 0
			for(l in 2:length(y)){
				value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
			}
			value = value/2
			value = value/max(x)
			pts = c(pts, value)
		}


		ir1 = c()
		irs = c()
		irn = c()

		for(k in 1:10){
			f2 = read.table(paste("DB/expfour/sqrt/500/", i, "/", k, sep=""))
			runtime = runtime + sum(as.numeric(f2[,4]))

			x = unique(f2[,2])
			pts2 = c()
			for (j in seq(1, nrow(f2), 100)){
				y = f2[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts2 = c(pts2, value)
			}

			f3 = read.table(paste("DB/expfour/n/500/", i, "/", k, sep=""))
			runtime = runtime + sum(as.numeric(f3[,4]))

			x = unique(f3[,2])
			pts3 = c()
			for (j in seq(1, nrow(f3), 100)){
				y = f3[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts3 = c(pts3, value)
			}

			w = read.table(paste("ireos_extension/Datasets/Real/weight/normalized_scores_median/", i, k, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			if(sum(w) != 0){
				ireos = sum((w/sum(w)) * pts)
				ir1 = c(ir1, ireos)
				ireos = sum((w/sum(w)) * pts2)
				irs = c(irs, ireos)
				ireos = sum((w/sum(w)) * pts3)
				irn = c(irn, ireos)
			}else{
				ir1 = c(ir1, 0)
				irs = c(irs, 0)
				irn = c(irn, 0)
			}
		}
		
		df = c(ir1o[which(!is.nan(ir1))]-ir1[which(!is.nan(ir1))], irso[which(!is.nan(irs))]-irs[which(!is.nan(irs))], irno[which(!is.nan(irn))]-irn[which(!is.nan(irn))]) 
		cat(" & ", round(mean(abs(df)),5), " $\\pm$ ", round(sd(df),5)," & ", sep="")
		runtime = runtime/30
		seconds = floor((runtime/(1000))%%60)
		minutes = floor((runtime/(1000*60))%%60)
		hours   = floor((runtime/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, sep="")
		cat(" \\\\ \n")
		
		#best = rbind(best, c(i, sort.list(ir1, dec=T)[1]))
=======
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
		colnames(M)[3] = "prec@n"
		colnames(M)[4] = "Max-F1"
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
>>>>>>> parent of bf87337... merry xmas
		#box = c(box, M[sort.list(ir, dec=T)[1],1])
	}
	#best
}

expfive <- function(){
	files = list.files("DB/expfive/")
	best = c()
	for(i in files){
		rt = 0
		cat(unique(unlist(lapply(strsplit(i, "_") , '[', 1))), " & ")
		f1 = read.table(paste("Compile/Real/", i, "/mcl1/1", sep=""))
		x = unique(f1[,2])
		pts = c()
		for (j in seq(1, nrow(f1), 100)){
			y = f1[j:(j+99),3]
			value = 0
			for(l in 2:length(y)){
				value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
			}
			value = value/2
			value = value/max(x)
			pts = c(pts, value)
		}
		rt = sum(as.numeric(f1[,4])) *10
		mcl = list.files(paste("Compile/Real/", i, sep=""))
		ir1o = c()
		irso = c()
		irno = c()
		for(k in 1:10){
			f2 = read.table(paste("Compile/Real/", i, mcl[2], k, sep="/"))
			x = unique(f2[,2])
			pts2 = c()
			for (j in seq(1, nrow(f2), 100)){
				y = f2[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts2 = c(pts2, value)
			}


			if(i != "Parkinson_withoutdupl_norm_05_v02"){
				f3 = read.table(paste("Compile/Real/", i, mcl[3], k, sep="/"))
			}else{
				f3 = read.table(paste("Compile/Real/", i, mcl[2], k, sep="/"))
			}
			x = unique(f3[,2])
			pts3 = c()
			for (j in seq(1, nrow(f3), 100)){
				y = f3[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts3 = c(pts3, value)
			}
		
			w = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/weight/normalized_scores_median/", i, k, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			if(sum(w) != 0){
				ireos = sum((w/sum(w)) * pts)
				ir1o = c(ir1o, ireos)
				ireos = sum((w/sum(w)) * pts2)
				irso = c(irso, ireos)
				ireos = sum((w/sum(w)) * pts3)
				irno = c(irno, ireos)
			}else{
				ir1o = c(ir1o, 0)
				irso = c(irso, 0)
				irno = c(irno, 0)
			}
			rt = rt + sum(as.numeric(f2[,4])) + sum(as.numeric(f3[,4]))
		}
		rt = rt/30
		seconds = floor((rt/(1000))%%60)
		minutes = floor((rt/(1000*60))%%60)
		hours   = floor((rt/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, sep="")


		f = read.table(paste("DB/expfive/", i, "/1", sep=""))
		f = f[order(f[,1], f[,2], f[,3]),]
		mcl = sort(unique(f[,1]))
		if(i == "Parkinson_withoutdupl_norm_05_v02"){
			mcl = c(mcl, mcl[2])
		}

		f1 = f[which(f[,1]==1),c(2,3,4,5)]
		runtime = c()

		size = max(f1[,1])
		gammaMax = max(f1[,2])
		pts = c()
		for(j in 0:size){
			ret = adaptiveQuads(0, gammaMax, 0.995, f1[which(f1[,1] == j),], T)
			#print(ret$errest)
			pts = c(pts, ret$norm)
			runtime = c(runtime, ret$runtime)
		}
		rtime = 0
		ir1 = c()
		for(k in 1:10){
			w = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/weight/normalized_scores_median/", i, k, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			ireos = sum((w/sum(w)) * pts)
			if(sum(w) != 0){
				ireos = sum((w/sum(w)) * pts)
				ir1 = c(ir1, ireos)
				rtime = rtime + sum(runtime[which(w>0)])
			}else{
				ir1 = c(ir1, 0)
			}
		}
		rtime = rtime/10
		seconds = floor((rtime/(1000))%%60)
		minutes = floor((rtime/(1000*60))%%60)
		hours   = floor((rtime/(1000*60*60)))
		cat(hours,":",minutes,":",seconds, " (", round((rtime*100)/rt, 2) ,"\\%) ", sep="")
		df = c(ir1o-ir1) 
		cat(" & ", round(mean(abs(df)),5), " \\\\ \n")

	}
}


realCorr1 <- function(){
	files = list.files("DB/exptwo/")
	files = files[!grepl("gauss",files)]
	for(i in files){
		dir.create(paste("Compile/Real/", i, sep = ""))
		print(i)
		sol = as.numeric(list.files(paste("DB/exptwo/", i, sep="")))
		for(j in sol){
			f = read.table(paste("DB/exptwo/", i, j, sep="/"))
			f = f[order(f[,1], f[,2], f[,3]),]
			mcl = unique(f[,1])
			for(m in mcl){
				if(m != 1){
					f1 = f[which(f[,1]==m),c(2,3,4,5)]
					temp = c()
					for(l in 0:max(f1[,1])){
						fx = f1[which(f1[,1] == l),]
						fx = fx[!duplicated(fx[,2]),]
						if(nrow(fx) != 100){
							cat("ERROR: ", j, m, l, nrow(fx), "\n")
						}
						temp = rbind(temp, fx)
					}
					dir.create(paste("Compile/Real/", i, "/mcl", m, sep = ""))
					write.table(temp, col.names=F, row.names=F, file=paste("Compile/Real/", i, "/mcl", m, "/", j, sep = ""), quote=F)
				}else if(j == 1){
					f1 = f[which(f[,1]==m),c(2,3,4,5)]
					temp = c()
					for(l in 0:max(f1[,1])){
						fx = f1[which(f1[,1] == l),]
						fx = fx[!duplicated(fx[,2]),]
						if(nrow(fx) != 100){
							cat("ERROR: ", j, m, l, nrow(fx), "\n")
						}
						temp = rbind(temp, fx)
					}
					dir.create(paste("Compile/Real/", i, "/mcl", m, sep = ""))
					write.table(temp, col.names=F, row.names=F, file=paste("Compile/Real/", i, "/mcl", m, "/", j, sep = ""), quote=F)
				}
			}
		}
	}
}
realCorr2 <- function(){
	best = c()
	files = list.files("Compile/Real/")
	for(i in files){
		cat(unique(unlist(lapply(strsplit(i, "_") , '[', 1))))
		f1 = read.table(paste("Compile/Real/", i, "/mcl1/1", sep=""))
		x = unique(f1[,2])
		pts = c()
		for (j in seq(1, nrow(f1), 100)){
			y = f1[j:(j+99),3]
			value = 0
			for(l in 2:length(y)){
				value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
			}
			value = value/2
			value = value/max(x)
			pts = c(pts, value)
		}

		mcl = list.files(paste("Compile/Real/", i, sep=""))
		ir1 = c()
		irs = c()
		irn = c()
		for(k in 1:10){
			f2 = read.table(paste("Compile/Real/", i, mcl[2], k, sep="/"))
			x = unique(f2[,2])
			pts2 = c()
			for (j in seq(1, nrow(f2), 100)){
				y = f2[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts2 = c(pts2, value)
			}

			if(i != "Parkinson_withoutdupl_norm_05_v02"){
				f3 = read.table(paste("Compile/Real/", i, mcl[3], k, sep="/"))
			}else{
				f2 = read.table(paste("Compile/Real/", i, mcl[2], k, sep="/"))
			}
			x = unique(f3[,2])
			pts3 = c()
			for (j in seq(1, nrow(f3), 100)){
				y = f3[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts3 = c(pts3, value)
			}
		
			w = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/weight/normalized_scores_median/", i, k, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
			if(sum(w) != 0){
				ireos = sum((w/sum(w)) * pts)
				ir1 = c(ir1, ireos)
				ireos = sum((w/sum(w)) * pts2)
				irs = c(irs, ireos)
				ireos = sum((w/sum(w)) * pts3)
				irn = c(irn, ireos)
			}else{
				ir1 = c(ir1, 0)
				irs = c(irs, 0)
				irn = c(irn, 0)
			}
		}
		
		cat(" & ", round(cor(ir1, seq(1,10), method="spearman"),3))
		cat(" & ", round(cor(irs, seq(1,10), method="spearman"),3))
		cat(" & ", round(cor(irn, seq(1,10), method="spearman"),3), " \\\\ \n")

		best = rbind(best, rbind(c(i, '1', sort.list(ir1, dec=T)[1]), c(i, 'sqrt', sort.list(irs, dec=T)[1]), c(i, 'n', sort.list(irn, dec=T)[1])))
		#print(rbind(c(i, '1', sort.list(ir1, dec=T)[1]), c(i, 'sqrt', sort.list(irs, dec=T)[1]), c(i, 'n', sort.list(irn, dec=T)[1])))
	}
	best
}


syntheticCorr <- function(){
		files = list.files("DB/exptwo/")
		files = files[grepl("gauss",files)]
		for(i in files){
			dir.create(paste("Compile/Synthetic/", i, sep = ""))
			print(i)
			f = read.table(paste("DB/exptwo/", i, "/1", sep=""))
			f = f[order(f[,1], f[,2], f[,3]),]
			f1 = f[which(f[,1]==1),c(2,3,4,5)]
			
			temp = c()
			for(l in 0:max(f1[,1])){
				fx = f1[which(f1[,1] == l),]
				fx = fx[!duplicated(fx[,2]),]
				if(nrow(fx) != 100){
					cat("ERROR: ", l,"\n")
				}
				temp = rbind(temp, fx)
			}
			dir.create(paste("Compile/Synthetic/", i, "mcl1", sep = "/"))
			write.table(temp, col.names=F, row.names=F, file=paste("Compile/Synthetic/", i, "mcl1", "1", sep = "/"), quote=F)

		}

		best = c()
		files = list.files("Compile/Synthetic/")
		for(i in files){
			cat(i)
			f1 = read.table(paste("Compile/Synthetic/", i, "/mcl1/1", sep=""))

			x = unique(f1[,2])
			pts = c()
			for (j in seq(1, nrow(f1), 100)){
				y = f1[j:(j+99),3]
				value = 0
				for(l in 2:length(y)){
					value = value + (x[l] - x[(l - 1)]) * (y[l - 1] + y[l])
				}
				value = value/2
				value = value/max(x)
				pts = c(pts, value)
			}

			ir1 = c()
			for(j in 1:10){
				w = read.table(paste("/home/henrique/ireos_extension/Datasets/Synthetic/weight/", i, j, sep = "/"), header = FALSE, sep = "\n", stringsAsFactors = FALSE)
				ireos = sum((w/sum(w)) * pts)
				ir1 = c(ir1, ireos)
			}
			
			cat(" &  ",cor(ir1, seq(1,10), method="spearman"), " & & \\ \\ \n")
			best = rbind(best, c(i, sort.list(ir1, dec=T)[1]))
		}
		best
}

tmp = readLines("DB/exptwo/Isolet/1")
for(i in 1:length(tmp)){
	if(length(strsplit(tmp[i], " ")[[1]]) != 5){
		cat("Removed: ", i, "\n")
		tmp = tmp[-i]
	}
}
write.table(tmp, col.names=F, row.names=F, file=paste("DB/exptwo/Isolet/1", sep=""), quote=F)


	mtry <- try(read.table("DB/expthree/InternetAds_withoutdupl_norm_05_v10/10"), silent = F)
	class(mtry)
	while(class(mtry) == "try-error"){
		line = as.numeric(strsplit(mtry[[1]], " ")[[1]][23])
		tmp = readLines("DB/exptwo/Isolet/1")
		tmp = tmp[-line]
		write.table(tmp, col.names=F, row.names=F, file="DB/exptwo/Isolet/1", quote=F)
		mtry <- try(read.table("DB/exptwo/Isolet/1"), silent = F)
	}

plotBoxPlot <- function(best){
	pdf("boxplot.pdf")
	par(mar=par("mar")+c(2,0,0,0))
	#datasets = list.files("Compile/Real")
	datasets = unique(best[,1])
	box = c()
	pts = c()
	if(real == T){
		for(i in datasets){
			infos = read.table(paste("/home/henrique/ireos_extension/Datasets/Real/solutions_info/", i, sep = ""), header = T, stringsAsFactors = F)
			box = rbind(box, cbind(infos[1,1], infos[,4]))
			pts = rbind(pts, infos[best[which(best[,1] == infos[1,1]),3],4])
			cat("\\multicolumn{1}{l|}{",unique(unlist(lapply(strsplit(infos[1,1], "_") , '[', 1))), "} & ", sep="")
			cat(round(min(infos[,4]),3), " & ", round(max(infos[,4]),3), " & \\multicolumn{1}{l|}{", round(mean(infos[,4]),3), "} & ", sep="")
			cat(round(infos[best[which(best[,1]==infos[1,1]),3][1],4],3), " & \\multicolumn{1}{l|}{", infos[best[which(best[,1]==infos[1,1]),3][1],2], "} & ", sep="")
			cat(round(infos[best[which(best[,1]==infos[1,1]),3][2],4],3), " & \\multicolumn{1}{l|}{", infos[best[which(best[,1]==infos[1,1]),3][2],2], "} & ", sep="")
			cat(round(infos[best[which(best[,1]==infos[1,1]),3][3],4],3), " & \\multicolumn{1}{l|}{", infos[best[which(best[,1]==infos[1,1]),3][3],2], "}  \\\\ \n", sep="")
		}
	}else{
		for(i in datasets){
			infos = read.table(paste("/home/henrique/ireos_extension/Datasets/Synthetic/solutions_info/", i, sep = ""), header = T, stringsAsFactors = F)
			p = rev(infos[,2])
			box = rbind(box, cbind(i, infos[,2]))
			pts = rbind(pts, p[as.numeric(best[which(best[,1] == i),2])])
		#	cat("\\multicolumn{1}{l|}{",i, "} & ", sep="")
			#cat(round(min(infos[,2]),3), " & ", round(max(infos[,2]),3), " & \\multicolumn{1}{l|}{", round(mean(infos[,2]),3), "} & ", sep="")
			#cat(round(p[as.numeric(best[which(best[,1]==i),3][1])],3), " & \\multicolumn{1}{l|}{", p[best[which(best[,1]==i),3][1],2], "} & ", sep="")
			#cat(" & \\multicolumn{1}{l|}{} & & \\multicolumn{1}{l|}{} \\\\ \n")
			#cat(round(infos[best[which(best[,1]==infos[1,1]),3][2],4],3), " & \\multicolumn{1}{l|}{", infos[best[which(best[,1]==infos[1,1]),3][2],2], "} & ", sep="")
			#cat(round(infos[best[which(best[,1]==infos[1,1]),3][3],4],3), " & \\multicolumn{1}{l|}{", infos[best[which(best[,1]==infos[1,1]),3][3],2], "}  \\\\ \n", sep="")
		}
	}
	#datasets = unlist(strsplit(datasets, "/"))[seq(9,length(datasets)*9,9)]
	datasets = unlist(lapply(strsplit(datasets, "_") , '[', 1))
	box = as.data.frame(box, stringsAsFactors = F)
	colnames(box)[1] = "datasets"
	colnames(box)[2] = "ROCAUC"
	box$ROCAUC = as.numeric(box$ROCAUC)
	boxplot(ROCAUC ~ datasets, data = box, xaxt="n", ylab = "ROC AUC", ylim = c(0,1))
	 #text(seq(0.1, (length(datasets)+0.5), length.out = length(datasets)), par("usr")[3]-0.1, datasets, xpd=TRUE, srt=45)
	text(1:length(datasets), par("usr")[3]-0.15, datasets, xpd=TRUE, srt=45)
	points(1:length(datasets), as.numeric(pts[,1]), pch = 0, cex = 1.5, col = 2)
	points(1:length(datasets), as.numeric(pts[,2]), pch = 3, cex = 1.5, col = 2)
	points(1:length(datasets), as.numeric(pts[,3]), pch = 4, cex = 1.5, col = 2)
	labNames = "Solution selected by ext-IREOS "
	legend('bottomright',legend= c(as.expression(bquote(.(labNames) ~ (m[cl] ~"="~1))), as.expression(bquote(.(labNames) ~ (m[cl] ~"="~sqrt()))),
	as.expression(bquote(.(labNames) ~ (m[cl] ~"="~n)))), pch = c(0,3,4),bty ="n", pt.cex = c(1.5,1.5,1.5), col = c(2,2,2))
	#legend('bottomright',legend= c(as.expression(bquote(.(labNames) ~ (m[cl] ~"="~1))), as.expression(bquote(.(labNames) ~ (m[cl] ~"="~n)))), pch = c(0,4),bty ="n", pt.cex = c(1.5,1.5), col = c(2,2))
	#legend('bottomright',legend= c(as.expression(bquote(.(labNames) ~ (m[cl] ~"="~1)))), pch = c(0),bty ="n", pt.cex = c(1.5), col = c(2))
	dev.off()
}

<<<<<<< HEAD
runEllipsesall <- function(path, mcl){
	pdf(paste("ellipse_", mcl, ".pdf", sep = ""), width=10, height=7)
	par(oma=c(0,0,0,0), mar=c(4,4,1,1), mgp=c(2,1,0), xaxs="i", yaxs="i")
	index = seq(0, 1, length.out = 200)
	col <- colorRampPalette(c("#67001F", "#B2182B", "#D6604D", "#F4A582", "#FDDBC7", "#FFFFFF", "#D1E5F0", "#92C5DE","#4393C3", "#2166AC", "#053061"))(200)

	files = list.files(paste(path, "/data/", sep = ""))
	datasets = unique(unlist(lapply(strsplit(files, "_") , '[', 1)))
	for(dataset in datasets){
		print(dataset)
		data = read.table(paste(path, "/difficultyPlots/", dataset, ".txt", sep=""), stringsAsFactors=F, h=T)
		dif = data$difficulty
		div = data$diversity

		mtry <- try(data[,paste('AdjIREOS.', mcl, sep="")], silent = TRUE)

		if(class(mtry) != "try-error"){
			ireos = data[,paste('AdjIREOS.', mcl, sep="")]
			cs = apply(as.matrix(ireos), 1, function(x) which(abs(x-index) == min(abs(x-index))))
			cs = col[unlist(lapply(cs, '[', 1))]
			cs[which(is.na(cs))] = 1
		}else{
			cs = rep(1, nrow(data))
		}

		xrange <- c(-.1,10.1)
		yrange <- c(-.07,4.07)
		move_labels = -0.15 # 0.04
		if(length(cs) > 1){
			dataEllipse(dif,div,xlim=xrange,ylim=yrange,levels=c(0.95),xlab=NA,ylab=NA,center.pch=4,center.cex=2.5,grid=FALSE, col = 1)
			points(dif, div, pch = 16, col = cs)
		}else{
			plot(dif, div, xlim=xrange,ylim=yrange,xlab=NA,ylab=NA, pch = 16, col = cs)
		}
		#text(dif+0.075, div-0.05, round(ireos,2), cex = 0.75)
		text(mean(dif)-move_labels,mean(div)-move_labels, dataset,cex=1.0,col=1)
		par(new=T)	
	}		
	title(xlab="Difficulty Score", ylab="Diversity Score",cex.lab=1.5)

	for(dataset in datasets){
		data = read.table(paste(path, "/difficultyPlots/", dataset, ".txt", sep=""), stringsAsFactors=F, h=T)
		dif = data$difficulty
		div = data$diversity

		mtry <- try(data[,paste('AdjIREOS.', mcl, sep="")], silent = TRUE)

		if(class(mtry) != "try-error"){
			ireos = data[,paste('AdjIREOS.', mcl, sep="")]
			cs = apply(as.matrix(ireos), 1, function(x) which(abs(x-index) == min(abs(x-index))))
			cs = col[unlist(lapply(cs, '[', 1))]
			cs[which(is.na(cs))] = 1
		}else{
			cs = rep(1, nrow(data))
		}

		xrange <- c(-.1,10.1)
		yrange <- c(-.07,4.07)
		move_labels = -0.15 # 0.04
		if(length(cs) > 1){
			dataEllipse(dif,div,xlim=xrange,ylim=yrange,levels=c(0.95),xlab=NA,ylab=NA,center.pch=4,center.cex=2.5,grid=FALSE, col = 1)
			points(dif, div, pch = 16, col = cs)
		}else{
			plot(dif, div, xlim=xrange,ylim=yrange,xlab=NA,ylab=NA, pch = 16, col = cs)
		}
		#text(dif+0.075, div-0.05, round(ireos,2), cex = 0.75)
		text(mean(dif)-move_labels,mean(div)-move_labels, dataset,cex=1.0,col=1)
		title(xlab="Difficulty Score", ylab="Diversity Score",cex.lab=1.5)
	}		

	dev.off()
}

runConcatone <- function(path){
	files = list.files("ext")
	files = files[grepl(x=files, "ir_")]
	for(i in files){
		print(i)
		ext = read.table(paste("ext/", i, sep = ""), stringsAsFactors=F)
		if(ext[nrow(ext),] == "------------------------------------"){
			mcl = as.numeric(strsplit(rev(strsplit(i, "_")[[1]])[1], ".log")[[1]])
			ext = ext[(nrow(ext)-10):nrow(ext),]
			dataset = unlist(strsplit(ext[1],"/"))[length(unlist(strsplit(ext[1],"/")))]
			AdjIREOS = as.numeric(ext[4])
			IREOS = as.numeric(ext[6])
			ttest = as.numeric(ext[8])
			ztest = as.numeric(ext[10])
			prettyname = unique(unlist(lapply(strsplit(dataset, "_") , '[', 1)))
			data = read.table(paste(path,"/difficultyPlots/", prettyname, ".txt", sep=""), stringsAsFactors=F, h=T)
			n = sum(read.table(paste(path,"/results_NOGLOSH	/", dataset, "/data", sep=""), stringsAsFactors=F, nrows=1)[,-1])
			if(mcl == 1){
				data[which(data$Dataset == dataset),'AdjIREOS.1'] = AdjIREOS
				data[which(data$Dataset == dataset),'IREOS.1'] = IREOS
				data[which(data$Dataset == dataset),'ttest.1'] = ttest
				data[which(data$Dataset == dataset),'ztest.1'] = ztest
			}else if(mcl == n){
				data[which(data$Dataset == dataset),'AdjIREOS.n'] = AdjIREOS
				data[which(data$Dataset == dataset),'IREOS.n'] = IREOS
				data[which(data$Dataset == dataset),'ttest.n'] = ttest
				data[which(data$Dataset == dataset),'ztest.n'] = ztest
				if(prettyname == "Parkinson"){
					data[which(data$Dataset == dataset),'AdjIREOS.sqrt'] = AdjIREOS
					data[which(data$Dataset == dataset),'IREOS.sqrt'] = IREOS
					data[which(data$Dataset == dataset),'ttest.sqrt'] = ttest
					data[which(data$Dataset == dataset),'ztest.sqrt'] = ztest
				}
			}else{
				data[which(data$Dataset == dataset),'AdjIREOS.sqrt'] = AdjIREOS
				data[which(data$Dataset == dataset),'IREOS.sqrt'] = IREOS
				data[which(data$Dataset == dataset),'ttest.sqrt'] = ttest
				data[which(data$Dataset == dataset),'ztest.sqrt'] = ztest
			}
			write.table(data, paste(path,"/difficultyPlots/", prettyname, ".txt", sep=""), col.names=T, row.names=F)
		}
	}
}

library(car)
library(FNN)
=======
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
		pdf("correlation_sep1.pdf")
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
			ntabelona[(k+2), 2] = "Prec@n"
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

		p1 = qplot(x=Measure, y=Dataset, data=ntabelona, fill=Value, geom="tile")  + geom_text(aes(Measure, Dataset, label = Value), color = "black", size = 4)

		p1 = p1 + guides(fill=guide_legend(title="Spearman")) +theme_bw() + theme(panel.border = element_blank(), panel.grid.major = element_blank(),
			panel.grid.minor = element_blank(), axis.line = element_blank(), axis.ticks = element_blank()) + xlab("") + ylab("") + scale_fill_gradientn(colours = col, limits=c(-1, 1), guide=FALSE) + guides(fill=FALSE)
		p1
		dev.off()

		pdf("correlation_sepn.pdf")
		datasets = pts[,1]
		np = pts[,c(1,7,9,11,13)]
		ntabelona = data.frame()
		k = 1
		for(i in 1:nrow(np)){
			ntabelona[k, 1] = np[i,1]
			ntabelona[(k+1), 1] = np[i,1]
			ntabelona[(k+2), 1] = np[i,1]
			ntabelona[(k+3), 1] = np[i,1]
			ntabelona[k, 2] = "AUC ROC"
			ntabelona[(k+1), 2] = "AP"
			ntabelona[(k+2), 2] = "Prec@n"
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

		p2 = qplot(x=Measure, y=Dataset, data=ntabelona, fill=Value, geom="tile")  + geom_text(aes(Measure, Dataset, label = Value), color = "black", size = 4)

		p2 = p2 + guides(fill=guide_legend(title="Spearman")) +theme_bw() + theme(panel.border = element_blank(), panel.grid.major = element_blank(),
			panel.grid.minor = element_blank(), axis.line = element_blank(), axis.ticks = element_blank()) + xlab("") + ylab("")+ scale_fill_gradientn(colours = col, limits=c(-1, 1)) + guides(fill=FALSE)
		p2
		dev.off()
	}

>>>>>>> parent of bf87337... merry xmas
library(ggplot2)
library(foreign)
library(fields)
library(parallel)
library(reshape2)
library(pROC)
library(corrplot)
source("RL/Utils.R")
source("/home/henrique/Downloads/DAMI-package/scripts/shared.R")
cores = 50
path = "/home/henrique/FullData/"
files = list.files(paste(path, "/synth-batch1/", sep = ""))
files = sub('\\.csv$', '', files)

datasets = unique(unlist(lapply(strsplit(files, "_") , '[', 1)))
<<<<<<< HEAD
=======
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



>>>>>>> parent of bf87337... merry xmas

runConcatone(path)
runEllipsesall(path, '1')
runEllipsesall(path, 'sqrt')
runEllipsesall(path, 'n')


