Extractor ADDON
================

This addon allows users to extract layers very easily.

author: @fvanderbiest

Typical configuration to include in your GEOR_custom.js file:

    {
        "id": "extractor_0",
        "name": "Extractor",
        "title": {
            "en": "Extractor",
            "es": "Extractor",
            "fr": "Extracteur"
        },
        "description": {
            "en": "This addon allows one to download data from the layers visible on the map",
            "es": "This addon allows one to download data from the layers visible on the map",
            "fr": "Cet addon permet de télécharger les données des couches visibles sur la carte"
        },
        "roles": [
            "ROLE_EXTRACTORAPP"
        ],
        "options": {
            "showWindowOnStartup": false,
            "srsData": [
                ["EPSG:4326", "WGS84 (EPSG:4326)"],
                ["EPSG:3857", "Spherical Mercator (EPSG:3857)"]
            ],
            "defaultSRS": "EPSG:4326",
            "defaultVectorFormat": "shp", // must be one of shp, mif, tab, kml
            "defaultRasterFormat": "geotiff", // must be one of geotiff, tiff
            "defaultRasterResolution": 50 // in centimeters
        }
    }

The above options are the defaults. Feel free to customize their values.

If ```showWindowOnStartup``` is set to true, the extractor window pops up as soon as the addon is loaded.
