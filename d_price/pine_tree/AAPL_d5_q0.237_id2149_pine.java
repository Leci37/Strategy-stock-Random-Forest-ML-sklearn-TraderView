
//@version=5
strategy("konk_AAPL_q0.205", overlay=true, margin_long=100, margin_short=100, pyramiding=5)
decision_tree_0(azul, marron, verde, media, azul_mean, verde_mean, marron_mean, verde_azul, verde_media, media_azul) =>
	var float ret = -1 // # DecisionTreeRegressor(criterion='friedman_mse', max_depth=5, max_features=0.7,
	if( azul_mean <= -0.193724 )
		if( marron <= 0.561418 )
			if( media_azul <= 57.1468 )
				ret := 1.846154
			if( media_azul > 57.1468 )
				ret := 1.111111
		if( marron > 0.561418 )
			if( verde_mean <= 105.688 )
				if( azul_mean <= -7.50721 )
					if( verde <= -22.3816 )
						ret := 1.555556
					if( verde > -22.3816 )
						ret := 0.955752
				if( azul_mean > -7.50721 )
					if( azul <= -1.49649 )
						ret := 0.576087
					if( azul > -1.49649 )
						ret := 0.870968
			if( verde_mean > 105.688 )
				if( media_azul <= 108.375 )
					if( verde <= 130.603 )
						ret := 1.629630
					if( verde > 130.603 )
						ret := 1.133333
				if( media_azul > 108.375 )
					if( media_azul <= 112.105 )
						ret := 0.545455
					if( media_azul > 112.105 )
						ret := 0.916667
	if( azul_mean > -0.193724 )
		if( media_azul <= 95.8661 )
			if( azul <= 40.1892 )
				if( media <= 52.4629 )
					if( marron_mean <= 44.048 )
						ret := 1.193396
					if( marron_mean > 44.048 )
						ret := 1.555556
				if( media > 52.4629 )
					if( media <= 63.4882 )
						ret := 0.827160
					if( media > 63.4882 )
						ret := 1.149912
			if( azul > 40.1892 )
				if( media_azul <= -18.881 )
					ret := 1.333333
				if( media_azul > -18.881 )
					if( media_azul <= 5.9799 )
						ret := 0.222222
					if( media_azul > 5.9799 )
						ret := 1.111111
		if( media_azul > 95.8661 )
			if( verde_azul <= 100.68 )
				if( azul_mean <= 8.55422 )
					ret := 1.866667
				if( azul_mean > 8.55422 )
					ret := 1.583333
			if( verde_azul > 100.68 )
				if( azul <= 4.78502 )
					if( media_azul <= 105.561 )
						ret := 1.025000
					if( media_azul > 105.561 )
						ret := 1.470588
				if( azul > 4.78502 )
					if( azul <= 6.66746 )
						ret := 1.882353
					if( azul > 6.66746 )
						ret := 1.272727
	
    ret //return
pvi = 0.0
nvi = 0.0

tprice = ohlc4
lengthEMA = input.int(255, minval=1)
m = input(15)
source = hlc3

// Pececillos
pvi := volume > volume[1] ? nz(pvi[1]) + (close - close[1]) / close[1]: nz(pvi[1])
pvim = ta.ema(pvi, m)
pvimax = ta.highest(pvim, 90)
pvimin = ta.lowest(pvim, 90)
oscp = (pvi - pvim) * 100 / (pvimax - pvimin)
// Tiburones
nvi := volume < volume[1] ? nz(nvi[1]) + (close - close[1]) / close[1]: nz(nvi[1])
nvim = ta.ema(nvi, m)
nvimax = ta.highest(nvim, 90)
nvimin = ta.lowest(nvim, 90)
azul = (nvi - nvim) * 100 / (nvimax - nvimin)
// Money Flow Index
upper_s = math.sum(volume * (ta.change(source) <= 0 ? 0: source), 14)
lower_s = math.sum(volume * (ta.change(source) >= 0 ? 0: source), 14)
xmf = 100.0 - 100.0 / (1.0 + upper_s / lower_s)
// Bollinger
mult = input(2.0)
basis = ta.sma(tprice, 25)
dev = mult * ta.stdev(tprice, 25)
upper = basis + dev
lower = basis - dev
OB1 = (upper + lower) / 2.0
OB2 = upper - lower
BollOsc = (tprice - OB1) / OB2 * 100
xrsi = ta.rsi(tprice, 14)
calc_stoch(src, length, smoothFastD) =>
    ll = ta.lowest(low, length)
    hh = ta.highest(high, length)
    k = 100 * (src - ll) / (hh - ll)
    ta.sma(k, smoothFastD)

stoc = calc_stoch(tprice, 21, 3)
marron = (xrsi + xmf + BollOsc + stoc / 3) / 2
verde = marron + oscp
media = ta.ema(marron, m)
azul_mean = ta.sma(azul, 5)
verde_mean = ta.sma(verde, 5)
marron_mean = ta.sma(marron, 5)

verde_azul = verde - azul
verde_media = verde - media
media_azul = media - azul
media_marron = media - marron
bandacero = 0

var float stop = na
var float limit1 = na
var float limit2 = na
// https://stackoverflow.com/questions/64524742/pine-script-tradingview-how-to-move-a-stop-loss-to-the-take-profit-level
percent2points(percent) =>
    strategy.position_avg_price * percent / 100 / syminfo.mintick
// sl & tp in % %
sl = percent2points(input(2.92, title="stop loss %%"))
tp1 = percent2points(input(1.12, title="take profit 1 %%"))
tp2 = percent2points(input(2.31, title="take profit 2 %%"))
tp3 = percent2points(input(3.91, title="take profit 3 %%"))
activateTrailingOnThirdStep = input(false,title="activate trailing on third stage (tp3 is amount, tp2 is offset level)")
curProfitInPts() =>
    if strategy.position_size > 0
        (high - strategy.position_avg_price) / syminfo.mintick
    else if strategy.position_size < 0
        (strategy.position_avg_price - low) / syminfo.mintick
    else
        0
calcStopLossPrice(OffsetPts) =>
    if strategy.position_size > 0
        strategy.position_avg_price - OffsetPts * syminfo.mintick
    else if strategy.position_size < 0
        strategy.position_avg_price + OffsetPts * syminfo.mintick
    else
        0
calcProfitTrgtPrice(OffsetPts) =>
    calcStopLossPrice(-OffsetPts)
getCurrentStage() =>
    var stage = 0
    if strategy.position_size == 0 
        stage := 0
    if stage == 0 and strategy.position_size != 0
        stage := 1
    else if stage == 1 and curProfitInPts() >= tp1
        stage := 2
    else if stage == 2 and curProfitInPts() >= tp2
        stage := 3
    stage
stopLevel = -1.
profitLevel = calcProfitTrgtPrice(tp3)

// based on current stage set up exit
// note: we use same exit ids ("x") consciously, for MODIFY the exit's parameters
curStage = getCurrentStage()
float op_operation = decision_tree_0(azul, marron, verde, media, azul_mean, verde_mean, marron_mean, verde_azul, verde_media, media_azul)
if (op_operation <= 1.0)
    if curStage == 1
        stopLevel := calcStopLossPrice(sl)
        strategy.exit("x", loss = sl, profit = tp3, comment = "sl or tp3")
    else if curStage == 2
        stopLevel := calcStopLossPrice(0)
        strategy.exit("x", stop = stopLevel, profit = tp3, comment = "breakeven or tp3")
    else if curStage == 3
        stopLevel := calcStopLossPrice(-tp1)
        strategy.exit("x", stop = stopLevel, profit = tp3, comment = "tp1 or tp3")
    else
        strategy.cancel("x")
// https://stackoverflow.com/questions/64524742/pine-script-tradingview-how-to-move-a-stop-loss-to-the-take-profit-level

// LUIS
if (op_operation >= 1.68) // buy
    stop := close * 0.965
    limit1 := close * 1.03
    limit2 := close * 1.02
    strategy.entry("x", strategy.long, 1, stop=stop, comment="in")

if (op_operation <= 0.1) // sell
    strategy.close("x", comment = "under Le1")
