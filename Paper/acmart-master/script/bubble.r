bubble <- function(){
	dataset  = read.table("/home/henrique/Documents/journal/acmart-master/data/knn-outlier_order.txt", stringsAsFactors=F)
	dataset = dataset[,-1]
	dataset[,4] = as.numeric(substr(dataset[,3],13,100))
	dataset[,3] = "inlier"
	dataset[1:5,3] = "outlier"
	file = "/home/henrique/Documents/journal/acmart-master/figs/bubble.pdf"
	radius <- sqrt( dataset[,3]/ pi )
	pdf(file)
	par(mgp=c(10,10,10))
	par(mar=c(0.2,0.2,0.2,0.2))
	plot(dataset[,1:2], cex=0.8)
	#symbols(dataset[,1], dataset[,2], circles=dataset[,4], inches=max(dataset[,4]), cex=1.5, xlim=c(-0,0.3), ylim=c(0.5,0.8))
	#symbols(dataset[which(dataset[,3]=="outlier"),1], dataset[which(dataset[,3]=="outlier"),2], circles=dataset[which(dataset[,3]=="outlier"),4], inches=max(dataset[,4]), fg="red", add=T, cex=1.5)
	#text(dataset[which(dataset[,3]=="outlier"),1], dataset[which(dataset[,3]=="outlier"),2], round(dataset[which(dataset[,3]=="outlier"),4], dig=2), cex=3)
	points(dataset[which(dataset[,3]=="outlier"),1], dataset[which(dataset[,3]=="outlier"),2], col=2, pch=15, cex=1.5)
	dev.off()
	
}


plotData <- function(){
		data = read.table("/home/henrique/Documents/journal/acmart-master/data/ilx3")
		seps = read.table("/home/henrique/Documents/journal/acmart-master/data/mahalanobis")
		file = "/home/henrique/Documents/journal/acmart-master/figs/data.pdf"
		pdf(file)
		par(mgp=c(10,10,10))
		par(mar=c(4,4,4,4))
		plot(data, cex=0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		
		points(data[101,], pch = 15, col = 4, cex = 1.8)
		points(data[102,], pch = 16, col = 3, cex = 1.8)
		points(data[99,], pch = 18, col = 2, cex = 2.2)

		legend('bottomright', c("More obvious outlier","Less obvious outlier","Not obvious outlier"), pch = c(15,16,18), col=c(4,3,2), bty ="n", pt.cex = c(1.8,1.8,2.2))
		dev.off()

		sc1 = c(1, 0.77, 0.56)
		sc2 = c(1, 0.56, 0.77)
		sc3 = c(0.77, 1, 0.56)
		sc4 = c(0.77, 0.56, 1)
		sc5 = c(0.56, 1, 0.77)
		sc6 = c(0.56, 0.77, 1)
		
		file = "/home/henrique/Documents/journal/acmart-master/figs/sep_curves.pdf"
		pdf(file)
		#par(mgp=c(10,10,10))
		par(mar=c(4,4,4,4))
		x = 99
		i = seq(1,nrow(seps),x+1)
		j = c(101, 102, 99)
		plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
		points(seps[i[j[2]]:(i[j[2]]+x), 2], seps[i[j[2]]:(i[j[2]]+x), 3], type = "l", lty = 2)
		points(seps[i[j[3]]:(i[j[3]]+x), 2], seps[i[j[3]]:(i[j[3]]+x), 3], type = "l", lty = 3)
		legend('bottomright', c("More obvious outlier","Less obvious outlier","Not obvious outlier"), lty = c(1,2,3),bty ="n")

		dev.off()

		file = "/home/henrique/Documents/journal/acmart-master/figs/avg_sep_curves.pdf"
		pdf(file)
		#par(mgp=c(10,10,10))
		par(mar=c(4,4,4,4))
		xs = seps[i[j[2]]:(i[j[2]]+x), 2]
		ys1 = (seps[i[j[1]]:(i[j[1]]+x), 3]*1 + seps[i[j[2]]:(i[j[2]]+x), 3]*0.75 + seps[i[j[3]]:(i[j[3]]+x), 3]*0.51)/ (1+0.75+0.51)
		ys2 = (seps[i[j[1]]:(i[j[1]]+x), 3]*1 + seps[i[j[2]]:(i[j[2]]+x), 3]*0.51 + seps[i[j[3]]:(i[j[3]]+x), 3]*0.75)/ (1+0.75+0.51)
		ys3 = (seps[i[j[1]]:(i[j[1]]+x), 3]*0.75 + seps[i[j[2]]:(i[j[2]]+x), 3]*1 + seps[i[j[3]]:(i[j[3]]+x), 3]*0.51)/ (1+0.75+0.51)
		ys4 = (seps[i[j[1]]:(i[j[1]]+x), 3]*0.75 + seps[i[j[2]]:(i[j[2]]+x), 3]*0.51 + seps[i[j[3]]:(i[j[3]]+x), 3]*1)/ (1+0.75+0.51)
		ys5 = (seps[i[j[1]]:(i[j[1]]+x), 3]*0.51 + seps[i[j[2]]:(i[j[2]]+x), 3]*1 + seps[i[j[3]]:(i[j[3]]+x), 3]*0.75)/ (1+0.75+0.51)
		ys6 = (seps[i[j[1]]:(i[j[1]]+x), 3]*0.51 + seps[i[j[2]]:(i[j[2]]+x), 3]*0.75 + seps[i[j[3]]:(i[j[3]]+x), 3]*1)/ (1+0.75+0.51)

		plot(xs, ys1, type="l", lty=1, ylim=c(0,1), xlab=expression(gamma), ylab = expression(bar(p)))
		#points(xs, ys2, type="l", lty=2)
		#points(xs, ys3, type="l", lty=3)
		#points(xs, ys4, type="l", lty=4)
		#points(xs, ys5, type="l", lty=2)
		points(xs, ys6, type="l", lty = 2)
		legend('bottomright', c("Truly best solution","False best solution"), lty = c(1,2),bty ="n")

		dev.off()
}