import pandas as pd
import numpy as np
import sys

RESOURCE_PATH = '../observatory/src/main/resources/'

# ============ #
# STATION DATA #
# ============ # 

def run_station_checks(path = RESOURCE_PATH, show = True):

    print("WARNING: the station id's are cast to floats; so the lead 0 is cutoff and they won't map 1-to-1")
    station_names = ['std_id', 'wban_id', 'lat', 'lon']
    station_df = pd.read_csv(path + 'stations.csv', names = station_names, header = None)
    station_df['id'] = station_df.std_id.astype(str).str.cat(station_df.wban_id.astype(str), sep = "-")
    station_df.drop(columns = ['std_id', 'wban_id'], inplace = True)
    
    station_df = station_df.dropna(subset = ['lat', 'lon'])
    station_df = station_df[(station_df.lat != 0.0) & (station_df.lon != 0.0)]

    print("station records = ", station_df.shape[0])
    print("lon range = ", (np.min(station_df.lon), np.max(station_df.lon)))
    print("lat range = ", (np.min(station_df.lat), np.max(station_df.lat)))
    if show:
        print(station_df.head())
        print("ten random records:")
        print(station_df.iloc[100:110])
    return station_df

# ================= # 
# TEMPERATURE DATA  #
# ================= #

def run_temperature_checks(path = RESOURCE_PATH, year = 1990 , show = True):
    # use coalesce on columns
    temp_names = ['std_id','wban_id', 'month', 'day', 'temperature'] 
    temp_df = pd.read_csv(RESOURCE_PATH + str(year) + '.csv', names = temp_names, header = None)

    temp_df = temp_df.loc[temp_df.temperature.between(-150, 150, inclusive = True)]
    temp_df['temp_C'] = (temp_df.temperature - 32) / 1.8
    temp_df = temp_df.astype({'std_id': int, 'wban_id': int})
    temp_df['id'] = temp_df.std_id.astype(str).str.cat(temp_df.wban_id.astype(str), sep = "-")
    temp_df.drop(columns = ['std_id', 'wban_id'], inplace = True)

    print("length of 1990 records = ", temp_df.shape[0])
    print("avg. temp of 1990 (C) = ", np.mean(temp_df.temp_C))
    # highest temp day (pick out station)
    # pick out most frequent station(s)
    # check if certain station code is dropped 

    if show:
        print(temp_df.head())
    return temp_df
        
# =========== #
# JOINED DATA #
# =========== # 

def run_joined_checks(path = RESOURCE_PATH, year = 1900, show = True):

    station_df = run_station_checks(path, False)
    temp_df = run_temperature_checks(path, year, False) 
    temp_df.set_index(['std_id', 'wban_id'], inplace = True)
    station_df.set_index(['std_id', 'wban_id'], inplace = True)
    joined_df = station_df.join(temp_df)

    if show:
        print(joined_df.head())
    return joined_df

# ==== #
# MAIN #
# ==== # 

if __name__ == '__main__':

    if sys.argv[1] == 'station':
        run_station_checks()
    elif sys.argv[1] == 'temperature':
        run_temperature_checks()
    elif sys.argv[1] == 'joined':
        run_joined_checks()
    elif sys.argv[1] == 'all':
        run_station_checks()
        run_temperature_checks()
        run_joined_checks()
    else:
        print('that\'s not a valid check parameter')
